package swagger.grails.model
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
class SwaggerParameter {
    String name
    String paramType
    String dataType

    boolean getRequired() {
        paramType in ["path", "body"]
    }
}
