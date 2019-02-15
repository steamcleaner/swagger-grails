package swagger.grails.model

import com.thoughtworks.paranamer.BytecodeReadingParanamer
import com.thoughtworks.paranamer.CachingParanamer
import com.thoughtworks.paranamer.Paranamer
import grails.web.mapping.UrlMapping
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.ClassUtils
import org.grails.core.DefaultGrailsControllerClass
import swagger.grails.SwaggerBuilderHelper

import java.lang.reflect.Method

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

        Method method = controllerClass.clazz.methods.findAll {
            it.name == actionName
        }.sort {
            -it.genericParameterTypes.size()
        }.head()

        if (method.parameterCount == 0)
            return []

        Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer())
        List<String> paramNames = paranamer.lookupParameterNames(method)

        List<SwaggerParameter> parameters = (1..method.parameterCount).collect { int index ->
            Class<?> type = method.parameters[index - 1].getType()
            boolean isPrimitiveOrString = (ClassUtils.isPrimitiveOrWrapper(type) || type == String.class)
            String fieldName = paramNames[index - 1]
            String dataType = {
                if (isPrimitiveOrString && type.simpleName != "String")
                    return type.simpleName
                else if (isPrimitiveOrString && type.simpleName == "String")
                    return "string"
                else
                    return type.name
            }()
            String paramType = "body"

            if (isPrimitiveOrString && pathParams.contains(fieldName))
                paramType = "path"
            else if (isPrimitiveOrString && !pathParams.contains(fieldName))
                paramType = "query"

            new SwaggerParameter(name: fieldName, dataType: dataType, paramType: paramType)
        } as List<SwaggerParameter>

        pathParams.each {
            if (!(it in parameters*.name))
                parameters << new SwaggerParameter(name: it, dataType: "string", paramType: "path")
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
}
