package uk.gov.justice.digital.hmpps.welcometoprison.model.basm.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException

@JsonDeserialize(using = JsonApiDeserializer::class)
data class JsonApiResponse<T>(val payload: List<T>)

typealias InclusionKey = Pair<JsonNode?, JsonNode?>
typealias Inclusions = Map<InclusionKey, JsonNode>

private fun inclusionKey(node: JsonNode) = node["type"] to node["id"]
private fun inclusions(node: JsonNode) = node.get("included")?.associateBy { inclusionKey(it) } ?: emptyMap()

class JsonApiDeserializer(private val valueType: JavaType? = null) : JsonDeserializer<Any>(), ContextualDeserializer {

  @Throws(JsonMappingException::class)
  override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> =
    JsonApiDeserializer(ctxt.contextualType.bindings.typeParameters[0])

  @Throws(IOException::class)
  override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Any {
    val node = parser.codec.readTree<ObjectNode>(parser)
    val inclusions = inclusions(node)

    val convert = { it: JsonNode -> parser.codec.treeToValue(normalise(inclusions, it), valueType?.rawClass) }

    val result = when (val data = node.get("data")) {
      is ArrayNode -> data.map { convert(it) }
      else -> listOf(convert(data))
    }
    return JsonApiResponse(result)
  }

  /**
   * Move each attribute, up a layer to the root of the object
   * Replace each relationship with its included counterpart (if present) and migrate to root of the object
   */
  private fun normalise(inclusions: Inclusions, item: JsonNode): JsonNode {
    if (item !is ObjectNode) throw RuntimeException("$item is not an object node")

    val attributes = item.getAndRemove("attributes")
    val relationships = item.getAndRemove("relationships")

    attributes?.let { item.setAll<JsonNode>(attributes) }
    relationships?.let {
      it.fields().forEach { (name, values) -> item.set<JsonNode>(name, getRelation(inclusions, values["data"])) }
    }

    return item
  }

  /**
   * Check to see if data for relation is included in API response and if so normalise and return
   * Otherwise just return the existing info (most like just type and ID)
   */
  private fun getRelation(inclusions: Inclusions, data: JsonNode) =
    inclusions[inclusionKey(data)]?.let { normalise(inclusions, it) } ?: data

  private fun ObjectNode.getAndRemove(name: String): ObjectNode? {
    val field = this.get(name)?.let { it as ObjectNode }
    this.remove(name)
    return field
  }
}
