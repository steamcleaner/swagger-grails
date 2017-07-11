package swagger.grails

import io.swagger.models.Swagger
import io.swagger.servlet.Reader
import io.swagger.util.Json

class SwaggerService {
    SwaggerCache swaggerCache

    /**
     * Generates a String representation of the swagger annotated controllers and actions.
     * <br><br>
     * As a way to limit the load on the implementing application
     * the swagger spec is cached as long as no changes are detected
     * in the url mappings or any controller.
     *
     * @return Returns the built swagger spec as a String
     */
    String generate() {
        Json.mapper().writeValueAsString(
                swaggerCache.getOrElse { Swagger swagger ->
                    Reader.read(swagger, SwaggerBuilder.classes)
                }
        )
    }
}
