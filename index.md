---
layout: default
title:  "Welcome!"
---

WSB-Webapp is an HTTP message broker library with a light J2EE-Like webapp framework.
It builds on top of the [wsb-core](https://github.com/richnou/wsb-core "WSB-Core") library which handles the communication layer and message brokering.



# Warning

So far, this library not intended to be used as a Webapplication framework for big applications, 
but has been designed to be easily embedded to provide easy-to-design Web-based GUI for Scala/Java based ones.


It also does not provide compatibility with J2EE standards like servlets, but it could be added, it is only a matter of semantic.

   
# Main features

From our experience with J2EE frameworks like JSF (Apache MyFaces), Facelets etc... we put focus on supporting interesting features 
which enable creating flexible and powerful user interfaces.

The Scala Script webpage interface provides for example support for:

- Templating (UI composition)
- Page parts definition
- Partial rendering and re-rendering for page parts (in the style of JSF, using a reRender attribute)
- Websockets to integrate actions with Remote Procedure Calls

# Versions

<table class="table">
<thead>
    <tr>
        <th>Version</th>
        <th>Branch / Tag</th>
        <th>Build Status</th>
        <th>Maven Documentation</th>
    </tr>
</thead>
<tbody>
    <tr>
        <td>1.0.0-SNAPSHOT</td>
        <td>master</td>
        <td><a href='https://www.idyria.com/jenkins/job/wsb-webapp/'><img src='https://www.idyria.com/jenkins/buildStatus/icon?job=wsb-webapp'></a></td>
        <td><a href='./maven/1.0.0-SNAPSHOT/'>Here</a></td>
    </tr>
</tbody>
</table>


## Documentation

- The [getting-started](gettingstarted.html "Getting Started") page provides a quick-start tutorial

## Maven fast setup


1. Deployment repositories (not on maven central yet)

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
~~~~~~




2. Dependency

~~~~~~~ xml
<dependency>
    <groupId>com.idyria.osi.wsb</groupId>
    <artifactId>wsb-webapp</artifactId>
    <version>CHOOSE FROM TABLE</version>
</dependency>
~~~~~~~
