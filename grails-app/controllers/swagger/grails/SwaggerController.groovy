package swagger.grails

import io.swagger.util.Json
import swagger.grails.model.SwaggerApi

class SwaggerController {
    SwaggerService swaggerService

    def index() {

    }

    def api() {
        render(status: 200, contentType: "application/json", text: swaggerService.generate())
    }

    def internal() {
        render(status: 200, contentType: "application/json", text: Json.mapper().writeValueAsString(SwaggerApi.apis))
    }
}
