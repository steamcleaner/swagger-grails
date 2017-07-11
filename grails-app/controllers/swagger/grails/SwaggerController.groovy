package swagger.grails

import com.fasterxml.jackson.databind.ObjectMapper
import swagger.grails.model.SwaggerApi

class SwaggerController {
    ObjectMapper mapper = new ObjectMapper()
    SwaggerService swaggerService

    def index() {

    }

    def api() {
        render(status: 200, contentType: "application/json", text: swaggerService.generate())
    }

    def apiInternal() {
        render(status: 200, contentType: "application/json", text: mapper.writeValueAsString(SwaggerApi.apis))
    }
}
