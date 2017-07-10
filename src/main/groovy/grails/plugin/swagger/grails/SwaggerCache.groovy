package grails.plugin.swagger.grails

import groovy.util.logging.Slf4j
import io.swagger.models.Swagger

/**
 * Simple cache that is used to house a current and backup instance
 * of the current {@link Swagger} spec.
 * <br><br>
 * The onchange hook in the {@link SwaggerGrailsPlugin} class triggers the flush method.
 */
@Slf4j
class SwaggerCache {
    Swagger swagger

    private Swagger backup = null
    private boolean backupLive = false

    /**
     * Returns the already built instance of swagger if it exists.
     * <br><br>
     * Otherwise a new instance of {@link Swagger} is built and the result of
     * the supplied closure is stored in it.
     * <br><br>
     * If any errors occur while building the swagger spec, this
     * guy will return the backup version.
     *
     * @param closure
     * @return An instance of {@link Swagger}
     */
    Swagger getOrElse(Closure closure) {
        if (!backup || backupLive) {
            try {
                closure.call(swagger)
                backup = swagger
                backupLive = false
            } catch (Exception e) {
                log.error("swagger --:>  SWAGGER CACHE FAILURE -- using backup.")
                log.error(e.getMessage(), e)

                backupLive = true
                swagger = backup
            }
        }
        swagger
    }

    void flush() {
        swagger = null
    }
}
