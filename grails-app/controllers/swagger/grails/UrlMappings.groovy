package swagger.grails

class UrlMappings {
    static mappings = {
        "/"(controller: "swagger", action: "api")
    }
}
