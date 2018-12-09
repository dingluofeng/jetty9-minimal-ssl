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
 * Enabling Client Authentication
 * Get client certificate in the servlet.
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class Step5ClientCertificateAuthServer {

    public static void main(String[] args) throws Exception {

        // create jetty server
        final Server server = new Server();

        // create factory for ssl
        final SslContextFactory sslContextFactory = new SslContextFactory();

        // Set keystore file path
        sslContextFactory.setKeyStorePath(System.getProperty("user.dir") + "/mykeystore.jks");

        // Set keystorepassword
        sslContextFactory.setKeyStorePassword("mypassword");

        // Enabling client auth
        sslContextFactory.setWantClientAuth(true);

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

            final X509Certificate[] clientCerts = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
            final String cipherSuite = (String) req.getAttribute("javax.servlet.request.cipher_suite");
            final Integer keySize = (Integer) req.getAttribute("javax.servlet.request.key_size");
            final String idStr = (String) req.getAttribute("javax.servlet.request.ssl_session_id");

            final StringBuilder sb = new StringBuilder();

            if (clientCerts != null) {
                for (int i = 0; i < clientCerts.length; i++) {
                    final X509Certificate cert = clientCerts[i];
                    sb.append("cert[" + i + "] subjectDN=" + cert.getSubjectDN()).append("\n");
                }
            } else {
                sb.append("No certificate!").append("\n");
            }
            sb.append("cipherSuite=" + cipherSuite).append("\n");
            sb.append("keySize=" + keySize).append("\n");
            sb.append("idStr=" + idStr).append("\n");

            resp.setContentType("text/plain; charset=UTF-8");

            final PrintWriter out = resp.getWriter();

            out.println("Hello Servlet");
            out.println("Client Certificate Info");
            out.println(sb.toString());
            out.close();

        }
    }
}
