package grails.plugin.swagger.grails

import com.fasterxml.jackson.databind.ObjectMapper
import grails.util.Holders
import io.swagger.annotations.Api
import io.swagger.util.Json
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import org.springframework.context.MessageSource

trait SwaggerBuilderHelper {
    static MessageSource messageSource = Holders.applicationContext.getBean("messageSource")
    static ClassPool classPool = ClassPool.getDefault()
    static ObjectMapper mapper = Json.mapper()

    /**
     * Delegates work to:
     * <br><br>
     * <code>static CtClass buildCtClass(def generatedName, String className)</code>
     *
     * @param className {@link String}
     * @return {@link javassist.CtClass}
     */
    static CtClass buildCtClass(String className) {
        buildCtClass(className, className)
    }

    /**
     * Builds a CtClass instance.
     * <br><br>
     * First it checks to see if the ClassPool contains an instance of a
     * class with the generatedName, if that one doesn't exist it falls
     * back to using the className.
     * <br><br>
     * It will also unfreeze a class so that more changes can be applied
     * to it.
     *
     * @param generatedName {@link String}
     * @param className {@link String}
     * @return {@link javassist.CtClass}
     */
    static CtClass buildCtClass(def generatedName, String className) {
        CtClass ctClass = Optional
                .ofNullable(classPool.getOrNull(generatedName as String))
                .orElse(classPool.getCtClass(className))

        if (ctClass.isFrozen()) {
            classPool = new ClassPool()
            classPool.appendSystemPath()
            ctClass = classPool.getCtClass(className)
        }

        ctClass
    }

    /**
     * Returns a list of javassist Annotations that have a base package of io.swagger.annotations
     *
     * @param javaAssistProxy
     * @return List of {@link javassist.bytecode.annotation.Annotation}
     */
    static List<Annotation> fetchExistingSwaggerAnnotations(def javaAssistProxy) {
        javaAssistProxy.getAttributes().findAll {
            it.class == AnnotationsAttribute
        }.collect { AnnotationsAttribute attribute ->
            attribute.getAnnotations()
        }.flatten().findAll { Annotation it ->
            it.typeName.startsWith(Api.class.package.name)
        }
    }

    /**
     * Returns true if the given javassist proxy contains an annotation matching the given class
     *
     * @param javaAssistProxy Javassist Proxy
     * @param clazz {@link Class}
     * @return boolean
     */
    static boolean hasExistingAnnotation(def javaAssistProxy, Class<?> clazz) {
        javaAssistProxy.getAttributes().findAll {
            it.class == AnnotationsAttribute
        }.collect { AnnotationsAttribute attribute ->
            attribute.getAnnotations()
        }.flatten().find { Annotation it ->
            it.typeName == clazz.name
        }
    }
}