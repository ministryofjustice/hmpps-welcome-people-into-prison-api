# hmpps-welcome-people-into-prison-api
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://welcome-api-dev.prison.service.justice.gov.uk/swagger-ui.html)

This service provides:
* Welcome people into prison functionality for welcome-people-into-prison-ui 
* Body scan functionality for welcome-people-into-prison-ui

# Starting the service

Run the `WelcomeToPrisonApi.kt` main method with the `dev` profile.

Ensure that IntelliJ is configured to run and build the project using gradle rather than it's own build tool.

### Linting
 to run linting  ```./gradlew ktlintFormat```


### Project architecture
Kotlin based project with 

```uk.gov.justice.digital.hmpps.welcometoprison``` - all classes for Welcome People Into Prison domain   
```uk.gov.justice.digital.hmpps.bodyscan``` - all classes for Body Scan domain  
```uk.gov.justice.digital.hmpps.archunit``` - all test classes for guarding project structure


## Test coverage report

Run:

```
./gradlew koverHtmlReport
```

Then view output file for coverage report.
