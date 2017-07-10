package grails.plugin.swagger.grails

import grails.plugin.swagger.grails.model.SwaggerApi
import grails.validation.Validateable
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import javassist.CtClass
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ClassFile
import javassist.bytecode.ConstPool

@Slf4j
class SwaggerBuilder implements SwaggerBuilderHelper {

    /**
     *  Scrapes over the {@link grails.plugin.swagger.grails.model.SwaggerApi} information to build new classes
     *  with the proper Swagger annotations added on so that the Swagger
     *  library can generate the required JSON.
     *
     * @return a set of {@link Class} objects
     */
    static Set<Class> getClasses() {
        List<SwaggerApi> swaggerApis = SwaggerApi.apis
        SwaggerLoader swaggerLoader = new SwaggerLoader()

        swaggerApis.collect { swaggerApi ->
            CtClass ctClass = buildCtClass("grails.plugin.swagger.grails.generated.${swaggerApi.shortName}", swaggerApi.className)
            ClassFile classFile = ctClass.getClassFile()
            ConstPool constPool = classFile.getConstPool()

            if (!hasExistingAnnotation(classFile, Api.class)) {
                try {
                    classFile.addAttribute(swaggerApi.buildApiAnnotation(constPool))
                } catch (Exception e) {
                    log.error("""
                        Unable to attach @Api annotation to: ${swaggerApi.shortName}
                    """.stripIndent(), e)
                }
            }

            swaggerApi.swaggerOperations.collect { operation ->
                try {
                    ctClass.getDeclaredMethods().findAll { dm ->
                        dm.name == operation.actionName
                    }.each { method ->
                        AnnotationsAttribute attribute = operation.buildOperationAnnotation(method, constPool)
                        method.getMethodInfo().addAttribute(attribute)
                    }
                } catch (Exception e) {
                    log.error("""
                            Unable to attach @ApiOperation annotation to: ${operation.actionName} in ${swaggerApi.shortName}
                            ${operation}
                        """.stripIndent(), e)
                }

                operation.commandObjects
            }.flatten().each {
                addMixinToCommandOjbect(it as String)
            }

            ctClass.setName("grails.plugin.swagger.grails.generated.${swaggerApi.shortName}")
            ctClass.toClass(swaggerLoader, null)
        }
    }

    /**
     * Fetches an instance of a class by the given name to determine if it implements
     * the {@link Validateable} trait.
     * <br><br>
     * If the parameter does implement Validateable, then a mixin is added to the Swagger
     * {@link io.swagger.util.Json#mapper} so that it ignores the {@link grails.validation.Validateable#getErrors}
     * when marshalling to JSON.
     *
     * @param swaggerParameter {@link grails.plugin.swagger.grails.model.SwaggerParameter}
     */
    private static void addMixinToCommandOjbect(String className) {
        try {
            Class clazz = Class.forName(className)
            clazz.getInterfaces().findAll {
                it == Validateable.class
            }.each {
                mapper.addMixIn(clazz, SwaggerIgnoreErrorsMixin.class)
            }
        } catch (ClassNotFoundException e) {
            log.error("""
                Unable to apply jackson mixin for unknown class: $className
                Please verify all @ApiImplicitParam annotations reference valid classes.
            """.stripIndent(), e)
        }
    }
}
