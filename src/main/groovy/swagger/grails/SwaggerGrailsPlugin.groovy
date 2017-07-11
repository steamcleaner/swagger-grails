package swagger.grails

import grails.plugins.Plugin
import io.swagger.models.SecurityRequirement
import io.swagger.models.Swagger
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.In

class SwaggerGrailsPlugin extends Plugin {
    def grailsVersion = "3.1.5 > *"
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/views/notFound.gsp",
            "**/test/**"
    ]
    def title = "swagger-grails"
    def author = "steamcleaner"
    def authorEmail = ""
    def description = "Grails 3.x plugin that will generate and display Swagger documentation."
    def profiles = ['web']
    def documentation = "https://github.com/steamcleaner/swagger-grails"
    def watchedResources = [
            "file:./grails-app/controllers/**/*.groovy"
    ]
    def license = "APACHE"
    def scm = [url: "https://github.com/steamcleaner/swagger-grails"]

    Closure doWithSpring() {
        { ->
            swagger(Swagger) { bean ->
                basePath = grailsApplication.config.server.contextPath ?: null
                securityDefinitions = ["apiKey": new ApiKeyAuthDefinition("apiKey", In.HEADER)]
                security = [new SecurityRequirement().requirement("apiKey")]
            }

            swaggerCache(SwaggerCache) { bean ->
                swagger = ref('swagger')
            }
        }
    }

    void doWithDynamicMethods() {
    }

    void doWithApplicationContext() {
    }

    void onChange(Map<String, Object> event) {
        if (event.source)
            (event.ctx.getBean("swaggerCache") as SwaggerCache).flush()
    }

    void onConfigChange(Map<String, Object> event) {
    }

    void onShutdown(Map<String, Object> event) {
    }
}
