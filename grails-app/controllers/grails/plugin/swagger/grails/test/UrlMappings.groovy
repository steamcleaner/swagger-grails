package grails.plugin.swagger.grails.test

class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        "/authors"(resources: 'author')

        "/publishers"(resources: 'publisher')
        "/custom/mapping/for/publisher/$id"(controller: "publisher", action: "update", method: "PUT")

        "/api/book"(controller: "book", action: "index", method: "GET")
        "/api/book"(controller: "book", action: "save", method: "POST")
        "/api/book/$id"(controller: "book", action: "show", method: "GET")
        "/api/book/$id"(controller: "book", action: "delete", method: "DELETE")
        "/api/book/$id"(controller: "book", action: "update", method: "PUT")
        "/api/book/$id"(controller: "book", action: "patch", method: "PATCH")
        "/api/book/one/$arg1/two/$arg2/three/$arg3"(controller: "book", action: "custom", method: "GET")
    }
}
