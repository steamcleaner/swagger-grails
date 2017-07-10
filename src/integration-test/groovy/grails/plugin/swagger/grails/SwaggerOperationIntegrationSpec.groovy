package grails.plugin.swagger.grails

import grails.plugin.swagger.grails.model.SwaggerApi
import grails.plugin.swagger.grails.model.SwaggerOperation
import grails.test.mixin.integration.Integration
import spock.lang.Specification

@Integration
class SwaggerOperationIntegrationSpec extends Specification {
    void "test that operations are built correctly for actions that have mappings defined"() {
        when:
        List<SwaggerApi> swaggerApis = SwaggerApi.getApis()
        SwaggerApi swaggerApi = swaggerApis.find { it.tag == "Book Controller" }
        List<SwaggerOperation> swaggerOperations = swaggerApi.swaggerOperations

        SwaggerOperation index = swaggerOperations.find { it.actionName == "index" }
        SwaggerOperation save = swaggerOperations.find { it.actionName == "save" }
        SwaggerOperation show = swaggerOperations.find { it.actionName == "show" }
        SwaggerOperation delete = swaggerOperations.find { it.actionName == "delete" }
        SwaggerOperation update = swaggerOperations.find { it.actionName == "update" }
        SwaggerOperation patch = swaggerOperations.find { it.actionName == "patch" }
        SwaggerOperation custom = swaggerOperations.find { it.actionName == "custom" }

        then:
        index.httpMethod == "GET"
        index.nickname == "/api/book"

        save.httpMethod == "POST"
        save.nickname == "/api/book"

        show.httpMethod == "GET"
        show.nickname == "/api/book/{id}"

        delete.httpMethod == "DELETE"
        delete.nickname == "/api/book/{id}"

        update.httpMethod == "PUT"
        update.nickname == "/api/book/{id}"

        patch.httpMethod == "PATCH"
        patch.nickname == "/api/book/{id}"

        custom.httpMethod == "GET"
        custom.nickname == "/api/book/one/{arg1}/two/{arg2}/three/{arg3}"
    }

    void "test that operations are built correctly for actions that do not have mappings defined"() {
        when:
        List<SwaggerApi> swaggerApis = SwaggerApi.getApis()
        SwaggerApi swaggerApi = swaggerApis.find { it.tag == "Author Controller" }
        List<SwaggerOperation> swaggerOperations = swaggerApi.swaggerOperations

        SwaggerOperation index = swaggerOperations.find { it.actionName == "index" }
        SwaggerOperation save = swaggerOperations.find { it.actionName == "save" }
        SwaggerOperation show = swaggerOperations.find { it.actionName == "show" }
        SwaggerOperation delete = swaggerOperations.find { it.actionName == "delete" }
        SwaggerOperation update = swaggerOperations.find { it.actionName == "update" }
        SwaggerOperation patch = swaggerOperations.find { it.actionName == "patch" }
        SwaggerOperation custom = swaggerOperations.find { it.actionName == "custom" }

        then:
        index.httpMethod == "GET"
        index.nickname == "/authors"

        save.httpMethod == "POST"
        save.nickname == "/authors"

        show.httpMethod == "GET"
        show.nickname == "/authors/{id}"

        delete.httpMethod == "DELETE"
        delete.nickname == "/authors/{id}"

        update.httpMethod == "PUT"
        update.nickname == "/authors/{id}"

        patch.httpMethod == "PATCH"
        patch.nickname == "/authors/{id}"

        custom.httpMethod == "GET"
        custom.nickname == "/author/custom"
    }
}
