# Introduction

Topics to cover:
- DDD
- access control
- mongoDB as default; easy to swap to a relational DB


# Tooling

- tooling dependencies: java 11, gradle, lombok
- building and Docker dependencies

## Building
The build system is [Gradle](https://gradle.org/).

To build the entire application, including running unit and functional tests:

`./gradlew build`

(Note that functional tests share the same port number for embedded database as for the containerised database, if 
tests fail try running `./gradlew composeDown` first).
 
To run the application server, including starting up containers Postgres and MongoDB:

`./gradlew bootRun`

To create a docker image:

`./gradlew dockerBuild`

To see all available Gradle tasks for this project:

`./gradlew tasks`

### Semantic versioning
Versioning is automatically applied using git tags. Simply create a tag of, say, 1.2.0 and Gradle will be ale to create 
a docker container tagged with version 1.2.0-0 where "-0" is the number of commits since the 1.2.0 tag was applied. 
Each subsequent commit will increment the version (1.2.0-1, 1.2.0-2, ...1.2.0-N) until the next release is tagged.   

## Jupyter notebook
An initial [Jupyter notebook](https://jupyter.org/) can be found in 'doc/notebook'. It acts as an interactive 
reference for the API endpoints and should be in your development workflow. A perfect replacement for Postman! Use it to 
try out the application, today!

(The notebook uses the [IRuby](https://github.com/sciruby/iruby) for Jupyter so go ahead and get that set up first).       

## Swagger documentation
Swagger API documentation is automatically generated by [Springfox](https://springfox.github.io/springfox/docs/current/).

API documentation is accessible when running the application locally by visiting 
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

!!! TODO !!! swagger docs seem to be broken


## IntelliJ configuration
This project uses [Project Lombok](https://projectlombok.org/) to generate common boilerplate code.

The [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok/) is required for IntelliJ, else the code generated 
by the Lombok annotations will not be visible (and the project will be littered with red squiggle line errors). 


# Features

Out of the box, this application comes with basic support for creating organisations and users. That's it. It's a fully 
end-to-end event sourced system with distributed command processing. 

## Axon
Previously known as Axon Framework, [Axon](https://axoniq.io/) is a framework for implementing [DDD](https://dddcommunity.org/learning-ddd/what_is_ddd/) 
using [event sourced])https://martinfowler.com/eaaDev/EventSourcing.html) [aggregates](https://www.martinfowler.com/bliki/DDD_Aggregate.html) 
and [CQRS](https://www.martinfowler.com/bliki/CQRS.html.

## Endpoint access control

- annotations
- follow the pattern!
- "identifiable" entities (not aggregates, not projections) answer the actual question

### Distributed command processing
- distributed command processing using Hazelcast, removing need for Axon Server for scaling up

- what the server gives you and how we do command processing
- why our command processing works
 
### Command validation
- the importance of protecting the domain
- the programming pattern for automatic validation using marker interfaces and interceptors
- 

### Replays
- what they are, why they're so important
- subscribing event processors vs tracking event processors

## thumbnail support

## security support

## file support
- deduplication
- starter kit comes with a single strategy to push into mongo GridFS. Could be any blob storage, really.


## ETag HTTP headers
[ETag HTTP headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag) are enabled by default for all API 
endpoints via a shallow filter. The filter is unaware of any changes made to underlying data so while clients may use the
ETag to avoid unnecessary transfers and client side processing, there is no benefit to application server performance.
