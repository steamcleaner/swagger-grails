package swagger.grails

import grails.validation.Validateable
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import javassist.CtClass
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ClassFile
import javassist.bytecode.ConstPool
import swagger.grails.model.SwaggerApi

@Slf4j
class SwaggerBuilder implements SwaggerBuilderHelper {

    /**
     *  Scrapes over the {@link swagger.grails.model.SwaggerApi} information to build new classes
     *  with the proper Swagger annotations added on so that the Swagger
     *  library can generate the required JSON.
     *
     * @return the list of classes with proper Swagger annotations
     */
    static Set<Class> getClasses() {
        List<SwaggerApi> swaggerApis = SwaggerApi.apis

        swaggerApis.collect { swaggerApi ->
            CtClass ctClass = buildCtClass(generateName(swaggerApi.shortName), swaggerApi.className)
            ClassFile classFile = ctClass.getClassFile()
            ConstPool constPool = classFile.getConstPool()

            if (!hasExistingAnnotation(classFile, Api.class))
                classFile.addAttribute(swaggerApi.buildApiAnnotation(constPool))

            swaggerApi.swaggerOperations.collect { operation ->
                ctClass.getDeclaredMethods().findAll { dm ->
                    dm.name == operation.actionName
                }.each { method ->
                    AnnotationsAttribute attribute = operation.buildOperationAnnotation(method, constPool)
                    method.getMethodInfo().addAttribute(attribute)
                }

                operation.commandObjects
            }.flatten().each {
                addMixinToCommandOjbect(it as String)
            }

            ctClass.setName(generateName(swaggerApi.shortName))
            ctClass.toClass()
        }.findAll()
    }

    /**
     * Fetches an instance of a class by the given name to determine if it implements
     * the {@link Validateable} trait.
     * <br><br>
     * If the parameter does implement Validateable, then a mixin is added to the Swagger
     * {@link io.swagger.util.Json#mapper} so that it ignores the {@link grails.validation.Validateable#getErrors}
     * when marshalling to JSON.
     *
     * @param swaggerParameter {@link swagger.grails.model.SwaggerParameter}
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
