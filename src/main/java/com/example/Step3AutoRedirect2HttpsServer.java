package com.example;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Redirect to HTTPS server when requesting as a HTTP
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class Step3AutoRedirect2HttpsServer {

    public static void main(String[] args) throws Exception {

        // create jetty server
        final Server server = new Server();

        // create factory for ssl
        final SslContextFactory sslContextFactory = new SslContextFactory();

        // Set keystore file path
        sslContextFactory.setKeyStorePath(System.getProperty("user.dir") + "/mykeystore.jks");

        // Set keystorepassword
        sslContextFactory.setKeyStorePassword("mypassword");

        // config for https
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(443);
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(80);

        ServerConnector httpsConnector = new ServerConnector(server, sslContextFactory);
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

        securityHandler.setHandler(resourceHandler);

        // Set connectors
        server.setConnectors(new Connector[] { httpConnector, httpsConnector });
        server.setHandler(securityHandler);

        // Start server
        server.start();
        server.join();
    }
}
