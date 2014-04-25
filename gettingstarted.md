---
layout: page
title: Getting Started
example_folder: examples/1.0.x/gettingstarted
---


This Getting Started tutorial will drive you through creating a Scala project, embed and start a WSB instance, and add a webapplication framework on top.

- Version Used for this tutorial: 1.0.0(-SNAPSHOT)
- Full example project files: Branch gh-pages, folder: [{{ page.example_folder }}]({{ site.github.repository_url }}/tree/gh-pages/{{ page.example_folder }})

This tutorial can be fully run on the command line, it is up to you to add to project into Eclipse or another IDE. 

## Create a Scala project


You can use your preferred build system here, but we always use maven, so here it goes:

1. Create a maven project

    Example's full pom.xml: [{{ page.example_folder }}/pom.xml]({{ site.github.repository_url }}/tree/gh-pages/{{ page.example_folder }}/pom.xml)

2. Make it Scala aware by using our predefined configuration (or skip this step if you can do this by yourself)

    a. Add the OSI repositories to your pom.xml  
   
    
    ~~~~~~~~~~ xml
    <!-- Repositories to find OSI packages -->
    <!-- ############################################## -->
    <pluginRepositories>
        <pluginRepository>
            <id>sonatype</id>
            <name>Sonatype OSS Snapshots Repository</name>
            <url>http://oss.sonatype.org/content/groups/public</url>
        </pluginRepository>
        <!-- For old snapshots, please use groupId `com.jnaerator` and the following 
            repo -->
        <pluginRepository>
            <id>nativelibs4java-repo</id>
            <url>http://nativelibs4java.sourceforge.net/maven</url>
        </pluginRepository>
        <pluginRepository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>idyria.central</id>
            <name>plugins-release</name>
            <url>http://www.idyria.com/access/osi/artifactory/libs-release</url>
        </pluginRepository>
        <pluginRepository>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <id>idyria.snapshots</id>
            <name>plugins-snapshot</name>
            <url>http://www.idyria.com/access/osi/artifactory/libs-snapshot</url>
        </pluginRepository>
    </pluginRepositories>
    <repositories>
        <repository>
            <id>sonatype</id>
            <name>Sonatype OSS Snapshots Repository</name>
            <url>http://oss.sonatype.org/content/groups/public</url>
        </repository>
        <!-- For old snapshots, please use groupId `com.jnaerator` and the following 
            repo -->
        <repository>
            <id>nativelibs4java-repo</id>
            <url>http://nativelibs4java.sourceforge.net/maven</url>
        </repository>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>idyria.central</id>
            <name>libs-release</name>
            <url>http://www.idyria.com/access/osi/artifactory/libs-release</url>
        </repository>
        <repository>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <id>idyria.snapshots</id>
            <name>libs-snapshot</name>
            <url>http://www.idyria.com/access/osi/artifactory/libs-snapshot</url>
        </repository>
    </repositories>
    ~~~~~~~~~~~~~~


    b. Add the OSI Scala configuration parent to your pom.xml


    ~~~~~~~~~~ xml
    <!-- use OSI Parent pom to enable Scala -->
    <!-- ################# -->
    <parent>
        <groupId>com.idyria.osi</groupId>
        <artifactId>project-scala</artifactId>
        <version>2.10.3.r2</version>
    </parent>
    ~~~~~~~~~~~   
    
3. Add the dependency

    ~~~~~~~~~~ xml
    <dependency>
        <groupId>com.idyria.osi.wsb</groupId>
        <artifactId>wsb-webapp</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    ~~~~~~~~~~~ 

4. Update the file structure

    Once Scala enabled, create following folders, and place your sources there:
    
- src/main/scala : For your main sources
- src/test/scala : For the unit tests


## Create an application

There is no maven plugin at the moment to start a server and deploy your new project in a standard way, because
the library is designed to first be embedded and configured depending on the application specific needs.

Full sources:  [{{ page.example_folder }}/src/main/scala/example/ExampleApp.scala]({{ site.github.repository_url }}/tree/gh-pages/{{ page.example_folder }}//src/main/scala/example/ExampleApp.scala)

1. Create a Scala application

    a. Create a package folder: src/main/scala/example
    b. Create an application: src/main/scala/example/ExampleApp.scala
    
    The initial content is:
    
    ~~~~~~~~~~ scala
    package example
    
    object ExampleApp extends App {
            
       println(s"Hello World!")
        
    }
    ~~~~~~~~~~~~~~

2. Check it is running (compile and run):

Run the following maven command, or use your IDE:

> mvn compile exec:java -Dexec.mainClass="example.ExampleApp" -DskipTests=true


## Embed the application server

Embedding the application server requires two steps:

1. Setup a WSB-Core engine, and add an HTTP network connector
2. Create a web application instance, and add it to the engine's message broker

This way, when receiving HTTP data on the network connection, the WSB library will send the parsed HTTP messages to the message broker, where 
the they will be caught by the Web application instance.

### Setup the WSB Library

This is easy, simply create an instance of a ``WSBEngine`` class:

- Import:

    ~~~~ scala
    import com.idyria.osi.wsb.core.WSBEngine
    ~~~~~~~~~~

- Instanciation:

    ~~~~~~ scala
    var engine = new WSBEngine()
    ~~~~~~~~
 
Now, add an I/O Connector, which will handle an HTTP connection and create HTTPMessage objects for the message broker

- Import (happy wildcard here)

    ~~~~ scala
    import com.idyria.osi.wsb.webapp.http.connector._
    ~~~~~~~~~~

- Create

    ~~~~ scala
    var port = 8087
    var connector = HTTPConnector(port)
    ~~~~~~~~~~

- Add
    
    ~~~~ scala
    engine.network.addConnector(connector)
    ~~~~~~~~~~

### Setup and bind a web application 

Now that the WSB engine is setup to deliver HTTP messages to be brokered, we need a Webapplication to handle these.

Basically, a Webapplication needs to know on which URL path it should match (/ , /myApp, etc...), where to find sources, configurations etc..
This is where standards like J2EE play a role to unify the way applications are setup.

To simplify configuration, we are going to use a ``MavenProjectWebApplication`` utility class, 
which sets up the file search path to maven standard's ``src/main/webapp``

- Import:

    ~~~~ scala
    import com.idyria.osi.wsb.webapp.MavenProjectWebApplication
    import java.io.File
    ~~~~~~~~~~

- Create

    ~~~~ scala
    var path = "/ExampleApp"
    var application = new MavenProjectWebApplication(new File("./pom.xml"), path)
    ~~~~~~~~~~
    
- Add

    ~~~~ scala
    engine.broker <= application
    ~~~~~~~~~~

### Start - Stop

Finally, we need to start and stop our application.
To make it easy, let's say that after starting, a simple key press on the terminal will close the application:

~~~~ scala
// Start the engine
//----------
engine.lInit
engine.lStart
  
// Stop the engine
//------------
println(s"Started on http://localhost:$port$path, press any key to stop....")
Console.readLine
engine.lStop
~~~~~~~~~~

### Add Files to be served

Files to be served must be added to the ``src/main/webapp`` folder

Let's try with a simple html:

- src/main/webapp/index-simple.html

~~~~~~~~~~~ html
<html>
    <head>
        <title>Hello World!</title>
    </head>
    <body>
        
        Hello World!
        
    </body>
</html>
~~~~~~~~~~~~~

- Point your browser to: [http://localhost:8087/ExampleApp/index-simple.html](http://localhost:8087/ExampleApp/index-simple.html)




## Use the Advanced Scala view format

Now that we can start a Web Application, we need a way to include dynamic code to generate the rendered view.

To do so, we don't support any Facelets/JSF like format, altough it could be added to the rendering chain.

Instead, we defined a file extension, which when matched executes the content as a closure to configure a ``View`` class instance.
This file can then use the available API to:

- templating: Customise an already existing ``View`` (templating)
- Define page "parts"
- Define the main view
    - Include page parts
- Embed controllers   
- ...

This format is detailed [here](view/view-intro.html







