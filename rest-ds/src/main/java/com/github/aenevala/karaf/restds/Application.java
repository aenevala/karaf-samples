package com.github.aenevala.karaf.restds;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.github.aenevala.karaf.shiro.AuthorizationExceptionMapper;
import com.github.aenevala.karaf.shiro.ShiroAnnotationResourceFilter;
import net.sf.ehcache.CacheManager;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.DestinationRegistryImpl;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by nevalaa on 29.9.2015.
 */
@Component
public class Application {

    private WebContainer webContainer;
    private CXFNonSpringServlet servlet;

    @Reference
    public void setWebContainer(WebContainer webContainer) {
        this.webContainer = webContainer;
    }

    @Activate
    public void activate(BundleContext cxt) throws ServletException, NamespaceException {
        CacheManager.getInstance().shutdown();

        // Create HTTP context
        HttpContext httpContext = webContainer.createDefaultHttpContext("GWA-REST");

        setupShiro(httpContext);

        // Create new destination registry for servlet so it hosts only services defined here
        DestinationRegistry registry = new DestinationRegistryImpl();
        // Create new CXF bus that is used
        Bus bus = CXFBusFactory.newInstance().createBus();
        bus.setId("GWA");
        // Register bus to OSGi
        cxt.registerService(Bus.class, bus, new Hashtable<String, Object>());

        // Create Http transport with destination registry
        HTTPTransportFactory transportFactory = new HTTPTransportFactory(registry);

        registerCxfServlet(httpContext, registry, bus);

        JAXRSServerFactoryBean bean = new JAXRSServerFactoryBean();

        bean.setBus(bus);
        bean.setDestinationFactory(transportFactory);
        bean.setStaticSubresourceResolution(true);
        bean.setServiceBeanObjects(new PersonService());

        bean.setFeatures(Arrays.asList(new LoggingFeature()));
        bean.setProvider(new JacksonJaxbJsonProvider());
        bean.setProvider(new ShiroAnnotationResourceFilter());
        bean.setProvider(new AuthorizationExceptionMapper());
        bean.setAddress("/asset");
        Server server = bean.create();


        JAXRSServerFactoryBean bean2 = new JAXRSServerFactoryBean();
        bean2.setBus(bus);
        bean2.setDestinationFactory(transportFactory);
        bean2.setServiceBeanObjects(new PersonService());
        bean2.setProvider(new JacksonJaxbJsonProvider());
        bean2.setProvider(new ShiroAnnotationResourceFilter());
        bean2.setProvider(new AuthorizationExceptionMapper());
        bean2.setAddress("/servicedelivery");
        bean2.create();


    }

    private void registerCxfServlet(final HttpContext httpContext, final DestinationRegistry registry, final Bus bus) throws ServletException, NamespaceException {
        servlet = new CXFNonSpringServlet(registry);
        servlet.setBus(bus);
        webContainer.registerServlet(servlet,"gwa-rest-services", new String[] {"/rest/*"}, new Hashtable(), 1,
                true, httpContext);
    }

    private void setupShiro(final HttpContext httpContext) {
        // Crete Shiro stuff
        EnvironmentLoaderListener listener = new EnvironmentLoaderListener();
        ShiroFilter filter = new ShiroFilter();
        webContainer.registerEventListener(listener, httpContext);
        Dictionary<String, String> initParams = new Hashtable<>();
        initParams.put("shiroConfigLocations", "classpath:shiro.ini");
        webContainer.registerFilter(filter, new String[] {"*"}, null, initParams, httpContext);
    }

}
