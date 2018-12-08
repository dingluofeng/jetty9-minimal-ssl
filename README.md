# Overview

Example code of HTTPS Server on Jetty9

It is licensed under [MIT](https://opensource.org/licenses/MIT).

# Enabling HTTPS(SSL/TLS) on Jetty9

## Source code    

```java
package com.example;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

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

```

## maven dependencies

```xml
  <dependencies>
        <dependency>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-webapp</artifactId>
            <version>9.4.14.v20181114</version>
        </dependency>
    </dependencies>
```


# Creating keytsotre and certificate(self-signing certificate)

## Create Keystore by keytool

Keytool is installed at '${JAVA_HOME}/bin'.
You can create a new keystore by keytol command below.

```shell
keytool -genkey -dname "cn=localhost, ou=Example div., o=Example Inc., l=Minato-ku, st=Tokyo, c=JP" -alias jetty -keystore mykeystore.jks -storepass mypassword -keypass mypassword -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 3650 -ext SAN=dns:localhost
```

Put mykeystore.jks in the working directory.

## Export certificate from keystore

keytool -export -alias jetty -storepass mypassword -file cert.crt -keystore mykeystore.jks

You can get 'cert.crt' and then import 'cert.crt' into your browser's 'trusted root CA' section for testing purpose.

# Run

## Create pom.xml and static contents

### pom.xml 

```pom.xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>org.riversun</groupId>
	<artifactId>jetty-minimal-ssl</artifactId>
	<version>1.0.0</version>
	<packaging>jar</packaging>
	<name>jetty-minimal-ssl</name>
	<description></description>

	<dependencies>
		<dependency>
			<groupId>org.eclipse.jetty</groupId>
			<artifactId>jetty-webapp</artifactId>
			<version>9.4.14.v20181114</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<version>1.6.0</version>
				<executions>
					<execution>
						<goals>
							<goal>java</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<mainClass>com.example.MinimalSSLServer</mainClass>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

### create '/htdocs' directory and index.html in '/htdocs'

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1>Hello jetty9-https</h1>

</body>
</html>

```


## run

```shell
mvn exec:java
```
