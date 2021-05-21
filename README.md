# swagger-grails

Generate Swagger documentation for your Grails 4 app.

## Built With

* Java 11
* Grails 4.0.3
* Swagger Core 1.5.17

## Installation

Add this line to the `repositories` block in your `build.gradle` file:

```groovy
maven { url "https://steamcleaner.jfrog.io/artifactory/grails-plugins" }
```

Add this line to the `dependencies` block in your `build.gradle` file:

```groovy
compile "org.grails.plugins:swagger-grails:0.4.0"
```

## Usage

The following __UrlMappings.groovy__ and __AuthorController.groovy__

```groovy
class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/authors"(resources: 'author')
    }
}
```

```groovy
class AuthorController {
    def index() {/*...*/ }

    def save(AuthorCommand authorCommand) {/*...*/ }

    def show(String id) {/*...*/ }

    def delete(String id) {/*...*/ }

    def update(String id) {/*...*/ }

    def patch(String id) {/*...*/ }

    def custom(String name, int age) {/*...*/ }
}

class AuthorCommand implements Validateable {
    String name
}
```

will generate Swagger documentation like this :

<p align="center">
    <img src="src/test/resources/author-controller.png?raw=true" />
</p>

This plugin will only generate the JSON representation of your endpoints. You'll need to implement your
own [swagger-ui](https://github.com/swagger-api/swagger-ui) to consume the JSON.

If your __UrlMappings__ file includes the default __"/$controller/$action?/$id?(.$format)?"__ mapping then the JSON will
be accessible by hitting __http://localhost:8080/swagger/api__. This endpoint can be customized by adding __"
/custom/swagger/endpoint"(controller: "swagger", action: "api")__ to your UrlMappings file.

Also, any Swagger annotations that are manually added to an action in a controller, will be used when generating the
Swagger documentation. So you could let the plugin do what it does by default and then enhance the actions with
responses, authorizations, etc.

## Configuration

The plugin also exposes the ability to customize the Swagger instance that is used.

The following __resources.groovy__ will create the default swagger instance that the plugin will use. It also sets the
security definition and security block. Together these two allow a header "apiKey"
to be attached to all calls from the front end.

```groovy
swagger(Swagger) {
    securityDefinitions = ["apiKey": new ApiKeyAuthDefinition("apiKey", In.HEADER)]
    security = [new SecurityRequirement().requirement("apiKey")]
}
```

Any of the fields on the Swagger object can be configured this way as well. For some more info on what can be configured
the [OpenAPI-Specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#schema) describes
what the Swagger object can contain.

## Running the plugin locally

The plugin source contains more examples of what types of configuration you can apply to controllers and actions :

##### Prerequisites

* Java 11
* Grails 4.0.3

##### Running

* Clone or download the repo
* Run `grails run-app`
* Navigate to `http://localhost:8080/`

