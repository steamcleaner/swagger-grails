package grails.plugin.swagger.grails

import grails.plugin.swagger.grails.test.PublisherController
import grails.test.mixin.integration.Integration
import grails.web.Action
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import spock.lang.Specification

import java.lang.annotation.Annotation
import java.lang.reflect.Method

@Integration
class SwaggerBuilderIntegrationSpec extends Specification {
    Class publisherController

    def setup() {
        Set<Class> classes = SwaggerBuilder.classes
        publisherController = classes.find { it.simpleName == PublisherController.class.simpleName }
    }

    void "test that the publisher controller has the correct @Api annotation"() {
        when:
        Api api = publisherController.annotations.find { Annotation it ->
            it.annotationType() == Api.class
        } as Api

        then:
        api
        api.value() == "/"
        api.tags().toList() == ["Custom Publisher"]
    }

    void "test that the index action on the publisher controller has the correct swagger annotations"() {
        when:
        Method index = publisherController.methods.find {
            it.name == "index" && !it.annotations*.annotationType().name.contains(Action.class.name)
        }

        then:
        index
        index.annotations.find {
            it.annotationType() == ApiOperation.class
        }.find { ApiOperation it ->
            it.nickname() == "/publishers" && it.httpMethod() == "GET"
        }
    }

    void "test that the show action on the publisher controller has the correct swagger annotations"() {
        when:
        Method show = publisherController.methods.find {
            it.name == "show" && !it.annotations*.annotationType().name.contains(Action.class.name)
        }

        then:
        show
        show.annotations.find {
            it.annotationType().name == ApiOperation.class.name
        }.find { ApiOperation it ->
            it.nickname() == "/publishers/{id}" && it.httpMethod() == "GET"
        }

        show.annotations.find {
            it.annotationType().name == ApiImplicitParams.class.name
        }.collect { ApiImplicitParams it ->
            it.value()
        }.flatten().find { ApiImplicitParam it ->
            it.dataType() == Long.class.name && it.name() == "id"
        }
    }

    void "test that the update action on the publisher controller has the correct swagger annotations"() {
        when:
        Method update = publisherController.methods.find {
            it.name == "update" && !it.annotations*.annotationType().name.contains(Action.class.name)
        }

        then:
        update
        update.annotations.find {
            it.annotationType().name == ApiOperation.class.name
        }.find { ApiOperation it ->
            it.nickname() == "/custom/mapping/for/publisher/{id}" && it.httpMethod() == "PUT"
        }

        update.annotations.find {
            it.annotationType().name == ApiImplicitParams.class.name
        }.collect { ApiImplicitParams it ->
            it.value()
        }.flatten().find { ApiImplicitParam it ->
            it.dataType() == Long.class.name && it.name() == "id"
        }
    }
}