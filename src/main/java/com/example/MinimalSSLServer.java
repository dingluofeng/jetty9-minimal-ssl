package com.example;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Minimal SSL Server on Jetty9
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class MinimalSSLServer {

    public static void main(String[] args) throws Exception {

        // create jetty server
        final Server server = new Server();

        // create factory for ssl
        final SslContextFactory sslContextFactory = new SslContextFactory();

        // Set keystore file path
        sslContextFactory.setKeyStorePath(System.getProperty("user.dir") + "/mykeystore.jks");

        // Set keystorepassword
        sslContextFactory.setKeyStorePassword("mypassword");

        // create connector for https
        final ServerConnector httpsConnector = new ServerConnector(server, sslContextFactory);
        httpsConnector.setPort(443);

        // create ResourceHandler for static contents
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");

        // Set connector
        server.setConnectors(new Connector[] { httpsConnector });

        // Set handler
        server.setHandler(resourceHandler);

        // Start server
        server.start();
        server.join();
    }
}
