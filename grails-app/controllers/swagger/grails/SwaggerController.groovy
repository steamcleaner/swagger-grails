package swagger.grails

import grails.util.Holders
import io.swagger.util.Json
import swagger.grails.model.SwaggerApi

class SwaggerController {
    SwaggerService swaggerService

    def api() {
        if (Holders.config.getProperty('swagger.active', Boolean.class)) {
            header("Access-Control-Allow-Origin", request.getHeader('Origin'))
            render(status: 200, contentType: "application/json", text: swaggerService.generate())
        } else
            render status: 404
    }

    def internal() {
        if (Holders.config.getProperty('swagger.active', Boolean.class)) {
            header("Access-Control-Allow-Origin", request.getHeader('Origin'))
            render(status: 200, contentType: "application/json", text: Json.mapper().writeValueAsString(SwaggerApi.apis))
        } else
            render status: 404
    }
}
