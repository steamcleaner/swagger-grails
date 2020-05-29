package swagger.grails.test

import grails.validation.Validateable

class AuthorController {
    def index() {
        render(status: 200, text: "OK")
    }

    def save(AuthorCommand authorCommand) {
        render(status: 200, text: "OK")
    }

    def show(String id) {
        render(status: 200, text: "OK")
    }

    def delete(String id) {
        render(status: 200, text: "OK")
    }

    def update(String id) {
        render(status: 200, text: "OK")
    }

    def patch(String id) {
        render(status: 200, text: "OK")
    }

    def custom(String name, int age) {
        render(status: 200, text: "OK")
    }
}

class AuthorCommand implements Validateable {
    String name
}
