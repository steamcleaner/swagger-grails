package swagger.grails

class UrlMappings {
    static mappings = {
        "/swagger"(controller: "swagger", action: "api")
    }
}
