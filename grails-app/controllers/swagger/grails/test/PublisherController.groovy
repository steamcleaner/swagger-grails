package swagger.grails.test

import grails.validation.Validateable
import io.swagger.annotations.*

@Api(value = "/", tags = ["Custom Publisher"])
class PublisherController {
    def index() {
        render(status: 200, text: "OK")
    }

    def show(Long id) {
        render(status: 200, text: "OK")
    }

    @ApiResponses([
            @ApiResponse(code = 400, message = "Good"),
            @ApiResponse(code = 500, message = "Server Error")
    ])
    def save(PublisherCommand publisherCommand) {
        render(status: 200, text: "OK")
    }

    @ApiOperation(value = "Update Publisher", nickname = "/custom/mapping/for/publisher/{id}", httpMethod = "PUT")
    def update(Long id, PublisherCommand publisherCommand) {
        render(status: 200, text: "OK")
    }

    @ApiImplicitParams([
            @ApiImplicitParam(name = "arg1", paramType = "query", required = false, value = "First Argument", dataType = "string"),
            @ApiImplicitParam(name = "arg2", paramType = "query", required = false, value = "Second Argument", dataType = "string"),
            @ApiImplicitParam(name = "publisherCommand", paramType = "body", required = true, value = "Publishers Command", dataType = "swagger.grails.test.PublisherCommand")
    ])
    def patch(String id, PublisherCommand publisherCommand) {
        render(status: 200, text: "OK")
    }
}

class PublisherCommand implements Validateable {
    List<AuthorCommand> authorCommands
}
