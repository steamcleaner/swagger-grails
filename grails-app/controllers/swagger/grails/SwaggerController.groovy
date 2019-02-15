package swagger.grails

import io.swagger.util.Json
import swagger.grails.model.SwaggerApi

class SwaggerController {
    SwaggerService swaggerService

    def api() {
        header("Access-Control-Allow-Origin", request.getHeader('Origin'))
        render(status: 200, contentType: "application/json", text: swaggerService.generate())
    }

    def internal() {
        header("Access-Control-Allow-Origin", request.getHeader('Origin'))
        render(status: 200, contentType: "application/json", text: Json.mapper().writeValueAsString(SwaggerApi.apis))
    }
}
