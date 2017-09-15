package swagger.grails

import groovy.util.logging.Slf4j
import io.swagger.models.Swagger
import org.codehaus.groovy.runtime.InvokerHelper

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
    private boolean flushed = true

    /**
     * Returns the already built instance of swagger if it exists.
     * Otherwise the closure is invoked and the result of that call
     * is cached locally.
     *
     * @param closure
     * @return An instance of {@link Swagger}
     */
    Swagger getOrElse(Closure closure) {
        if (!backup) {
            backup = new Swagger()
            InvokerHelper.setProperties(backup, swagger.properties)
        }

        if (flushed) {
            InvokerHelper.setProperties(swagger, backup.properties)

            try {
                closure.call(swagger)
                backup = swagger
            } catch (Exception e) {
                log.error(e.getMessage(), e)
                swagger = backup
            } finally {
                flushed = false
            }
        }
        swagger
    }

    void flush() {
        flushed = true
    }
}
