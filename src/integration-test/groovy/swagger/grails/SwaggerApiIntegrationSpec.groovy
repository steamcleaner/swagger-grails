package swagger.grails

import swagger.grails.test.AuthorController
import swagger.grails.test.BookController
import grails.test.mixin.integration.Integration
import spock.lang.Specification
import swagger.grails.model.SwaggerApi

@Integration
class SwaggerApiIntegrationSpec extends Specification {
    void "test that the SwaggerApi is built correctly"() {
        when:
        List<SwaggerApi> swaggerApis = SwaggerApi.getApis()
        SwaggerApi bookApi = swaggerApis.find { it.tag == "Book Controller" }
        SwaggerApi authorApi = swaggerApis.find { it.tag == "Author Controller" }

        then:
        BookController.class.name == bookApi.className
        "Book Controller" == bookApi.tag
        "BookController" == bookApi.shortName
        bookApi.swaggerOperations.size() == 7

        AuthorController.class.name == authorApi.className
        "Author Controller" == authorApi.tag
        "AuthorController" == authorApi.shortName
        authorApi.swaggerOperations.size() == 7
    }
}
