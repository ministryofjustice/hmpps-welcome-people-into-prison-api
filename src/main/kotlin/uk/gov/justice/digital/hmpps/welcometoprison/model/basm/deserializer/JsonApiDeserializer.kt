package uk.gov.justice.digital.hmpps.welcometoprison.model.basm.deserializer

import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode

@JsonDeserialize(using = JsonApiDeserializer::class)
data class JsonApiResponse<T>(val payload: List<T>)

typealias InclusionKey = Pair<JsonNode?, JsonNode?>
typealias Inclusions = Map<InclusionKey, JsonNode>

private fun inclusionKey(node: JsonNode) = node["type"] to node["id"]
private fun inclusions(node: JsonNode) = node.get("included")?.associateBy { inclusionKey(it) } ?: emptyMap()

class JsonApiDeserializer(private val valueType: JavaType? = null) : ValueDeserializer<Any>() {

  override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): ValueDeserializer<*> = JsonApiDeserializer(ctxt.contextualType.bindings.typeParameters[0])

  override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Any {
    val node: ObjectNode = ctxt.readTree(parser) as ObjectNode
    val inclusions = inclusions(node)

    val convert = { it: JsonNode ->
      ctxt.readTreeAsValue(normalise(inclusions, it, 0), valueType?.rawClass) as Any
    }

    val result: List<Any> = when (val data = node.get("data")) {
      is ArrayNode -> data.values().map { convert(it) }
      else -> listOf(convert(data))
    }
    return JsonApiResponse(result)
  }

  /**
   * Move each attribute, up a layer to the root of the object
   * Replace each relationship with its normalised included counterpart (if present) and migrate to root of the object
   * Limit recursion depth to prevent stack overflow and OOM with deeply nested structures
   */
  private fun normalise(inclusions: Inclusions, item: JsonNode, depth: Int): JsonNode {
    if (item !is ObjectNode) throw RuntimeException("$item is not an object node")

    val attributes = item.getAndRemove("attributes")
    val relationships = item.getAndRemove("relationships")

    attributes?.let { item.setAll(attributes) }
    relationships?.let {
      it.properties().asSequence()
        .filter { (_, values) -> values?.get("data") != null }
        .forEach { (name, values) ->
          item.set(name, getRelations(inclusions, values["data"], depth + 1))
        }
    }

    return item
  }

  /**
   * Check to see if data for relation is included in API response and if so normalise and return
   * Otherwise just return the existing info (most like just type and ID)
   */
  private fun getRelations(inclusions: Inclusions, data: JsonNode, depth: Int): JsonNode = if (data.size() > 2) {
    JsonMapper().valueToTree(data.map { getRelation(inclusions, it, depth) })
  } else {
    getRelation(inclusions, data, depth)
  }

  private fun getRelation(inclusions: Inclusions, data: JsonNode, depth: Int): JsonNode {
    val maxDepth = 10
    return if (depth >= maxDepth) {
      // At max depth, return just type and id to prevent OOM from circular references
      data
    } else {
      inclusions[inclusionKey(data)]?.let { normalise(inclusions, it, depth + 1) } ?: data
    }
  }

  private fun ObjectNode.getAndRemove(name: String): ObjectNode? {
    val field = this.get(name)?.let { it as ObjectNode }
    this.remove(name)
    return field
  }
}
