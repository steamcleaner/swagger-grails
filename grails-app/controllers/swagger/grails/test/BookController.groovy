package swagger.grails.test

import grails.validation.Validateable

class BookController {
    def index() {
        render(status: 200, text: "OK")
    }

    def save(BookCommand bookCommand) {
        render(status: 200, text: "OK")
    }

    def show(Long id) {
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

    def custom(String arg1, String arg2, String arg3, String arg4) {
        render(status: 200, text: "OK")
    }
}

class BookCommand implements Validateable {
    String title
}