package swagger.grails.model

import grails.web.mapping.UrlMapping
import groovy.util.logging.Slf4j
import org.grails.core.DefaultGrailsControllerClass
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodNode
import swagger.grails.SwaggerBuilderHelper

@Slf4j
class SwaggerParameterBuilder implements SwaggerBuilderHelper {

    /**
     * Using the supplied class and action name all of the path/query/body params
     * are found and mashed together.
     * any of the path params.
     *
     * @param controllerClass {@link org.grails.core.DefaultGrailsControllerClass}
     * @param actionName {@link String}
     * @param urlMapping {@link grails.web.mapping.UrlMapping}
     * @return List of path/query/body level swaggerParameters
     */
    static List<SwaggerParameter> buildSwaggerParameters(DefaultGrailsControllerClass controllerClass, String actionName, UrlMapping urlMapping) {
        List<String> pathParams = buildPathParams(urlMapping)
        ClassNode classNode = new ClassNode()
        ClassReader cr = new ClassReader(getClassForAsm(controllerClass.clazz.name))
        cr.accept(classNode, 0)

        List<SwaggerParameter> parameters = classNode.methods.find { MethodNode mn ->
            mn.name == actionName
        }.collect { MethodNode mn ->
            List<Type> argumentTypes = Type.getArgumentTypes(mn.desc).toList()

            argumentTypes.withIndex().collect { type, index ->
                String fieldName = (mn.localVariables as List<LocalVariableNode>).get(index + 1).name
                boolean primitive = argumentTypes[index].className in primitives
                String paramType = "body"

                if (primitive && pathParams.contains(fieldName))
                    paramType = "path"
                else if (primitive && !pathParams.contains(fieldName))
                    paramType = "query"

                new SwaggerParameter(name: fieldName, dataType: argumentTypes[index].className, paramType: paramType)
            }.inject([]) { list, param ->
                if (param.validate())
                    list << param
                else
                    logParameterValidationError(controllerClass.naturalName, actionName, param)

                list
            }
        }.flatten() as List<SwaggerParameter>

        pathParams.each {
            if (!(it in parameters*.name))
                parameters << new SwaggerParameter(name: it, dataType: "java.lang.String", paramType: "path")
        }

        parameters.sort {
            it.name
        }
    }

    /**
     * Finds all url data tokens that match the given regex.
     * An index is then added to this list that is used to find the corresponding
     * property name from the constraints object that is on the url mapping.
     * <br><br>
     * These params are used to determine if a method parameter needs to be
     * flagged as path param instead of a query/body parameter.
     *
     * @param urlMapping {@link grails.web.mapping.UrlMapping}
     * @return List of params that are on the path
     */
    private static List<String> buildPathParams(UrlMapping urlMapping) {
        if (!urlMapping)
            return []

        urlMapping.urlData.tokens.findAll {
            it ==~ /\(\*\)/
        }.withIndex().collect { String token, int index ->
            urlMapping.constraints[index].propertyName
        }
    }

    /**
     * Prints out validation errors in a human readable format
     *
     * @param controller Short name of controller
     * @param param {@link SwaggerParameter}
     */
    private static void logParameterValidationError(String controller, String actionName, SwaggerParameter param) {
        String replacement = "of class [class ${SwaggerParameter.class.name}] "
        String errors = param.errors.allErrors.collect { error ->
            "\t${messageSource.getMessage(error, null).replace(replacement, "")}"
        }.join("\n")

        log.error("""
        |$param failed validation @ $controller.$actionName()
        |$errors
        """.stripMargin())
    }

    private static def getClassForAsm(String className) {
        try {
            GrailsWebRequest webUtils = WebUtils.retrieveGrailsWebRequest()
            def request = webUtils.getCurrentRequest()
            request.servletContext.classLoader.getResourceAsStream(className.replace(".", "/") + ".class")
        } catch (Exception e) {
            className
        }
    }
}