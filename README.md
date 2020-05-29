# swagger-grails
Generate Swagger documentation for your Grails 4 app.

## Built With
* Java 11
* Grails 4.0.3
* Swagger Core 1.5.16

## Installation
Add this line to the `repositories` block in your `build.gradle` file:
    
    maven { url "https://dl.bintray.com/steamcleaner/plugins/" }

Add this line to the `dependencies` block in your `build.gradle` file:
    
    compile "org.grails.plugins:swagger-grails:0.4.0"
    
>>> 
The <code>swagger-servlet</code> dependency includes a logback-test.xml file, which will override
any logback file that you have declared in your app.  To circumvent this in the mean time, just include a logback-test.groovy file in your application that is a copy of the logback.groovy file.
>>>

## Usage
Given the following UrlMappings.groovy ...
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

... and the following AuthorController.groovy ...
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

... will generate Swagger documentation like this :

<p align="center">
    <img src="src/test/resources/author-controller.png?raw=true" />
</p>

Also any Swagger annotations that are manually added to actions in  controllers
will be used when generating the Swagger documentation. So you could let the
plugin do what it does by default and then enhance the actions with responses,
authorizations, etc...

## Configuration
The plugin also exposes the ability to customize the Swagger instance that is used.

The following resources.groovy ...
```groovy
    swagger(Swagger) {
        securityDefinitions = ["apiKey": new ApiKeyAuthDefinition("apiKey", In.HEADER)]
        security = [new SecurityRequirement().requirement("apiKey")]
    }
```
... will create the default swagger instance that the plugin will use.  It also sets the
 security definition and security block.  Together these two allow a header "apiKey"
 to be attached to all calls from the front end.
 
 Any of the fields on the Swagger object can be configured this way as well.  For some more
 info on what can be configured the [OpenAPI-Specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#schema) describes what the Swagger
 object can contain.

## Running the plugin locally
The plugin source contains more examples of what types of configuration you
can apply to controllers and actions :

##### Prerequisites
* Java 11
* Grails 4.0.3

##### Running
* Clone or download the repo
* Run `grails run-app`
* Navigate to `http://localhost:8080/`

