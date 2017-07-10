package grails.plugin.swagger.grails.model

import grails.validation.Validateable
import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 * Contains the information used to build swagger annotations for the inputs on an action.
 * <br><br>
 * For example:
 * <ul>
 *     <li>Path params</li>
 *     <li>Query params</li>
 *     <li>Command objects</li>
 * </ul>
 */
@Slf4j
@ToString(includes = ['name', 'paramType', 'dataType', 'required'], includePackage = false)
class SwaggerParameter implements Validateable {
    String name
    String paramType
    String dataType

    boolean getRequired() {
        paramType in ["path", "body"]
    }

    static constraints = {
        name nullable: false, blank: false
        paramType nullable: false, blank: false
        dataType nullable: false, blank: false
    }
}
