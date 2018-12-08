package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * HTTPS With Servlet.
 * Get client certificate.
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class Step4GetCertInfoHttpsServer {

    public static void main(String[] args) throws Exception {

        // create jetty server
        final Server server = new Server();

        // create factory for ssl
        final SslContextFactory sslContextFactory = new SslContextFactory();

        // Set keystore file path
        sslContextFactory.setKeyStorePath(System.getProperty("user.dir") + "/mykeystore.jks");

        // Set keystorepassword
        sslContextFactory.setKeyStorePassword("mypassword");

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(443);
        ServerConnector httpConnector = new ServerConnector(server,
                new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(80);

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        ServerConnector httpsConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        httpsConnector.setPort(443);

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        Constraint constraint = new Constraint();
        constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);

        securityHandler.addConstraintMapping(mapping);

        // create ResourceHandler for static contents
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");

        // Set connector
        server.setConnectors(new Connector[] { httpConnector, httpsConnector });

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.addServlet(ExampleServlet.class, "/test");

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(resourceHandler);
        handlerList.addHandler(servletContextHandler);

        securityHandler.setHandler(handlerList);

        server.setHandler(securityHandler);

        // Start server
        server.start();
        server.join();
    }

    @SuppressWarnings("serial")
    public static class ExampleServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            X509Certificate[] clientCerts = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");

            if (clientCerts != null && clientCerts.length > 0) {
                X509Certificate x509 = clientCerts[0];
                System.out.println("cert=" + x509.getSubjectDN());
            }

            resp.setContentType("text/plain; charset=UTF-8");

            final PrintWriter out = resp.getWriter();

            out.println("Hello Servlet");
            out.close();

        }
    }
}
