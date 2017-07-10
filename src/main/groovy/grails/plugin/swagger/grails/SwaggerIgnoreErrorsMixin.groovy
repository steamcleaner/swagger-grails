package grails.plugin.swagger.grails

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * See <a href="https://github.com/FasterXML/jackson-docs/wiki/JacksonMixInAnnotations">Jackson MixIn</a>
 */
@JsonIgnoreProperties("errors")
class SwaggerIgnoreErrorsMixin {}