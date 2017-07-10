package grails.plugin.swagger.grails

import grails.util.Holders
import grails.web.mapping.UrlMapping
import grails.web.mapping.UrlMappingsHolder
import org.grails.web.mapping.UrlMappingUtils

import javax.servlet.ServletContext

trait SwaggerMapping {
    private static UrlMappingsHolder urlMappingsHolder = UrlMappingUtils.lookupUrlMappings(Holders.servletContext as ServletContext)

    UrlMapping mappingForControllerAndAction(String controllerName, String action) {
        urlMappingsHolder.urlMappings.findAll {
            it.controllerName == controllerName
        }.find { UrlMapping urlMapping ->
            urlMapping.actionName == action
        }
    }
}