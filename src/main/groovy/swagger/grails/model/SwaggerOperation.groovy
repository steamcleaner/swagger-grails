package swagger.grails.model

import grails.web.mapping.UrlMapping
import groovy.transform.ToString
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ConstPool
import javassist.bytecode.annotation.*
import org.grails.core.DefaultGrailsControllerClass
import swagger.grails.SwaggerBuilderHelper
import swagger.grails.SwaggerMapping

import java.util.regex.Pattern

/**
 * Contains the information that allows swagger generation for action level annotations.
 */
@ToString(includes = ['value', 'nickname', 'httpMethod', 'actionName'], includePackage = false)
class SwaggerOperation implements SwaggerMapping, SwaggerBuilderHelper {
    private DefaultGrailsControllerClass controllerClass
    private UrlMapping urlMapping

    String value
    String nickname
    String httpMethod
    List<SwaggerParameter> swaggerParameters = []
    List<String> commandObjects = []

    SwaggerOperation(DefaultGrailsControllerClass controllerClass, String actionName) {
        this.controllerClass = controllerClass
        this.urlMapping = mappingForControllerAndAction(controllerClass.logicalPropertyName, actionName)
        this.value = actionName
        this.nickname = buildNickname()
        this.httpMethod = urlMapping?.httpMethod ?: genericHttpMethod
        this.swaggerParameters = SwaggerParameterBuilder.buildSwaggerParameters(controllerClass, actionName, this.urlMapping)
    }

    String getActionName() {
        value
    }

    /**
     * If no url mapping was supplied then the generic nickname is built and returned.
     * <br><br>
     * Otherwise the nickname is generated using the url data from the url mapping
     * for the supplied action.
     */
    private String buildNickname() {
        if (!urlMapping)
            return genericNickname

        String nickname = (urlMapping.urlData.urlPattern.split("/").findAll { it }.inject([0, []]) { List _list, piece ->
            if (piece ==~ /\(\*\)/ || piece ==~ Pattern.quote("(*)(.(*))?"))
                [_list[0] + 1, _list[1] << "{${urlMapping.constraints[_list[0]].propertyName}}"]
            else
                [_list[0], _list[1] << piece.replace("(.(*))?", "")]
        })[1].join("/")

        nickname.startsWith("/") ? nickname : "/$nickname"
    }

    /**
     * Uses the action name to determine the format for the nickname.
     * <br><br>
     * Some formatting can be inferred for action names that match the default
     * list of actions that grails recognizes.
     */
    private String getGenericNickname() {
        switch (actionName) {
            case "index":
            case "save":
                return "/${controllerClass.logicalPropertyName}"
            case "delete":
            case "show":
            case "update":
            case "patch":
                return "/${controllerClass.logicalPropertyName}/{id}"
            default:
                return "/${controllerClass.logicalPropertyName}/$actionName"
        }
    }

    /**
     * Uses the action name to determine the http method.
     * <br><br>
     * Http methods can be inferred for action names that match the default
     * list of actions that grails recognizes.
     */
    private String getGenericHttpMethod() {
        switch (value) {
            case "index": return "GET"
            case "save": return "POST"
            case "show": return "GET"
            case "delete": return "DELETE"
            case "update": return "PUT"
            case "patch": return "PATCH"
            default: return "GET"
        }
    }

    /**
     * TODO: Not sure if this is going to be needed...
     *
     * @return
     */
    private String getApiValuePath() {
        Optional
                .ofNullable(controllerClass.clazz.annotations.find { it.annotationType() == Api.class })
                .map { annotation -> annotation as Api }
                .map { api -> api.value() }
                .orElse("")
    }

    /**
     * Builds the api operation for the given swagger operation.
     *
     * @param ctClass {@link javassist.CtClass}
     * @param constPool {@link javassist.bytecode.ConstPool}
     */
    AnnotationsAttribute buildOperationAnnotation(CtMethod method, ConstPool constPool) {
        AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag)

        if (!hasExistingAnnotation(method.methodInfo, ApiOperation.class)) {
            Annotation annotation = new Annotation(ApiOperation.class.name, constPool)
            annotation.addMemberValue("value", new StringMemberValue(value, constPool))
            annotation.addMemberValue("nickname", new StringMemberValue(nickname, constPool))
            annotation.addMemberValue("httpMethod", new StringMemberValue(httpMethod, constPool))
            attribute.addAnnotation(annotation)
        }

        fetchExistingSwaggerAnnotations(method.methodInfo).each {
            attribute.addAnnotation(it)
        }

        if (swaggerParameters)
            attribute.addAnnotation(buildImplictParamsArrayAnnotation(method, constPool))

        attribute
    }

    /**
     * Builds the implicit parameters for controller actions given the swagger parameters for a swagger operation..
     *
     * @param method {@link javassist.CtMethod}
     * @param pool {@link javassist.bytecode.ConstPool}
     * @return {@link javassist.bytecode.annotation.Annotation}
     */
    Annotation buildImplictParamsArrayAnnotation(CtMethod method, ConstPool pool) {
        new Annotation(ApiImplicitParams.class.name, pool).with { _annotation ->
            _annotation.addMemberValue("value", new ArrayMemberValue(pool).with {
                it.setValue(buildImplicitParamAnnotations(method, pool))
                it
            })
            _annotation
        }
    }

    /**
     * The list of swaggerParameters is collected over in order to build the necessary
     * list of annotations to attach to the {@link ApiImplicitParams} annotation.
     *
     * @param method {@link javassist.CtMethod}
     * @param pool {@link javassist.bytecode.ConstPool}
     * @return an array of {@link javassist.bytecode.annotation.AnnotationMemberValue}
     */
    AnnotationMemberValue[] buildImplicitParamAnnotations(CtMethod method, ConstPool pool) {
        List<AnnotationMemberValue> memberValues = swaggerParameters.collect { SwaggerParameter swaggerParam ->
            if (swaggerParam.paramType == "body")
                commandObjects << swaggerParam.dataType

            new Annotation(ApiImplicitParam.class.name, pool).with {
                it.addMemberValue("name", new StringMemberValue(swaggerParam.name, pool))
                it.addMemberValue("paramType", new StringMemberValue(swaggerParam.paramType, pool))
                it.addMemberValue("required", new BooleanMemberValue(swaggerParam.required, pool))
                it.addMemberValue("value", new StringMemberValue(swaggerParam.name, pool))
                it.addMemberValue("dataType", new StringMemberValue(swaggerParam.dataType, pool))
                it
            }
        }.collect { Annotation annotation ->
            new AnnotationMemberValue(pool).with {
                it.setValue(annotation)
                it
            }
        }

        (fetchExistingSwaggerAnnotations(method.methodInfo).find {
            it.typeName == ApiImplicitParams.class.name
        }?.getMemberValue("value") as ArrayMemberValue)?.value?.each { AnnotationMemberValue it ->
            if (it.value.getMemberValue("paramType").toString() == "\"body\"")
                commandObjects << it.value.getMemberValue("dataType").toString().replaceAll("\"", "")

            memberValues << it
        }

        memberValues as AnnotationMemberValue[]
    }
}
