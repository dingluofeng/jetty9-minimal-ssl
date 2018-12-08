package com.example;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Supports both HTTP and HTTPS
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class Step2HttpAndHttpsServer {

    public static void main(String[] args) throws Exception {

        // create jetty server
        final Server server = new Server();

        // create factory for ssl
        final SslContextFactory sslContextFactory = new SslContextFactory();

        // Set keystore file path
        sslContextFactory.setKeyStorePath(System.getProperty("user.dir") + "/mykeystore.jks");

        // Set keystorepassword
        sslContextFactory.setKeyStorePassword("mypassword");

        // Connector for http
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(80);

        // Connector for https
        ServerConnector httpsConnector = new ServerConnector(server, sslContextFactory);
        httpsConnector.setPort(443);

        // create ResourceHandler for static contents
        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(System.getProperty("user.dir") + "/htdocs");

        // Set connector
        server.setConnectors(new Connector[] { httpConnector, httpsConnector });

        server.setHandler(resourceHandler);

        // Start server
        server.start();
        server.join();
    }
}
