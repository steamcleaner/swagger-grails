package swagger.grails

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import grails.util.Holders
import io.swagger.annotations.Api
import io.swagger.util.Json
import javassist.ClassPool
import javassist.CtClass
import javassist.LoaderClassPath
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource

trait SwaggerBuilderHelper {
    @JsonIgnore
    static ObjectMapper mapper = Json.mapper()
    @JsonIgnore
    static MessageSource messageSource = Holders.applicationContext.getBean("messageSource")
    @JsonIgnore
    static ClassPool classPool = loadClassPool()

    /**
     *
     *
     * @param swaggerApi
     * @return
     */
    static String generateName(String shortName) {
        "swagger.grails.generated.${RandomStringUtils.random(6)}.$shortName"
    }

    static ClassPool loadClassPool() {
        classPool = new ClassPool()
        classPool.appendSystemPath()

        try {
            GrailsWebRequest webUtils = WebUtils.retrieveGrailsWebRequest()
            def request = webUtils.getCurrentRequest()
            classPool.appendClassPath(new LoaderClassPath(request.servletContext.classLoader))
        } catch (Exception e) {
        }

        classPool
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
        Optional
                .ofNullable(classPool.getOrNull(generatedName as String))
                .orElse(classPool.getCtClass(className))
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
