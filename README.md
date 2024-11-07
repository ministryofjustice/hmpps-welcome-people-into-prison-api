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

### HMPPS-template-kotlin update
The project is sync with https://github.com/ministryofjustice/hmpps-template-kotlin  
Sync date: `16/10/2024` sync commit: `7426643351775e175fc5e77101fd9226fe66ef86`

#### How to sync the project with HMPPS-template-kotlin
Check if remote repository is added by `git remote -v`.
if repository is not present run `git remote add hmpps-kotlin-tamplate git@github.com:ministryofjustice/hmpps-template-kotlin.git`.
Fetch the newest changes from hmpps-kotlin-template calling `git fetch hmpps-kotlin-template`.
Find out missing commits https://github.com/ministryofjustice/hmpps-template-kotlin/commits/main/ following `sync commit` above.
Cherry Pick the changes by calling `git cherry-pick <<commit_id>>..<<commit_id>>` if you want to Cherry pick only one commit call `git cherry-pick <<commit_id>>`
Resolve the conflict and update **sync date** and **sync commit** in README.md to guide the next person on what was done.  