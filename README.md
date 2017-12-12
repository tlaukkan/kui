# KUI - Kotlin Web UI

Seed for Kotlin web UI projects provides the following features:

* User and group management
* Resource and log monitoring
* Key value and time series storage
    * Application layer encryption
    * Support for both SQL and NoSQL databases 

Technology stack:

* Web UI: Bootstrap 4, KotlinJS
* API: Rest (JSON / HTTPS)
* Web container: embedded Undertow
* Storage: JPA (PostgreSQL, H2...), Cassandra and DynamoDb 

## Modules

Project consists of the following modules:

* kui-client, programmatic API client providing currently example functions of resource monitor and log aggregation to web server.
* kui-common, shared API model and utility classes between kui-client and kui-web.
* kui-core, core implementation includes security, storage and utility classes.
* kui-web, embedded Undertow web server provides rest API and static asset loading.
    * ui, bootstrap 4 and KotlinJS web UI
    
## Running from IntelliJ Idea

Run kui-web/src/main/kotlin/org/kui/server/main.kt (with module directory as Working directory)

Browse to https://127.0.0.1:8443/

Default admin username: default.admin
Default admin password: password1234

