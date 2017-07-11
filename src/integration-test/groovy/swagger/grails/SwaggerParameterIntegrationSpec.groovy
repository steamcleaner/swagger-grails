package swagger.grails

import swagger.grails.model.SwaggerOperation
import swagger.grails.test.BookCommand
import grails.test.mixin.integration.Integration
import spock.lang.Specification
import swagger.grails.model.SwaggerApi
import swagger.grails.test.AuthorCommand

@Integration
class SwaggerParameterIntegrationSpec extends Specification {
    void "test that swagger parameters are built correctly for actions that have mappings defined"() {
        when:
        List<SwaggerApi> swaggerApis = SwaggerApi.getApis()
        SwaggerApi swaggerApi = swaggerApis.find { it.tag == "Book Controller" }
        List<SwaggerOperation> swaggerOperations = swaggerApi.swaggerOperations

        SwaggerOperation index = swaggerOperations.find { it.actionName == "index" }
        SwaggerOperation save = swaggerOperations.find { it.actionName == "save" }
        SwaggerOperation show = swaggerOperations.find { it.actionName == "show" }
        SwaggerOperation custom = swaggerOperations.find { it.actionName == "custom" }

        then:
        !index.swaggerParameters

        save.swaggerParameters.size() == 1
        save.swaggerParameters.head().paramType == "body"
        save.swaggerParameters.head().dataType == BookCommand.class.name

        show.swaggerParameters.size() == 1
        show.swaggerParameters.head().paramType == "path"
        show.swaggerParameters.head().dataType == Long.class.name

        custom.swaggerParameters.size() == 4
        custom.swaggerParameters.findAll { it.paramType == "path" && it.dataType == String.class.name }.size() == 3
        custom.swaggerParameters.findAll { it.paramType == "query" && it.dataType == String.class.name }.size() == 1
    }

    void "test that swagger parameters are built correctly for actions that do not have mappings defined"() {
        when:
        List<SwaggerApi> swaggerApis = SwaggerApi.getApis()
        SwaggerApi swaggerApi = swaggerApis.find { it.tag == "Author Controller" }
        List<SwaggerOperation> swaggerOperations = swaggerApi.swaggerOperations

        SwaggerOperation index = swaggerOperations.find { it.actionName == "index" }
        SwaggerOperation save = swaggerOperations.find { it.actionName == "save" }
        SwaggerOperation show = swaggerOperations.find { it.actionName == "show" }
        SwaggerOperation custom = swaggerOperations.find { it.actionName == "custom" }

        then:
        !index.swaggerParameters

        save.swaggerParameters.size() == 1
        save.swaggerParameters.head().paramType == "body"
        save.swaggerParameters.head().dataType == AuthorCommand.class.name

        show.swaggerParameters.size() == 1
        show.swaggerParameters.head().paramType == "path"
        show.swaggerParameters.head().dataType == String.class.name

        custom.swaggerParameters.size() == 2
        custom.swaggerParameters.findAll { it.paramType == "query" && it.dataType == String.class.name }.size() == 1
        custom.swaggerParameters.findAll { it.paramType == "query" && it.dataType == "int" }.size() == 1
    }
}
