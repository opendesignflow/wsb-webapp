---
layout: page
title: Introduction to the View format
example_folder: examples/1.0.x/view-intro
webapp_folder: examples/1.0.x/view-intro/src/main/webapp
---

This tutorial shows basic usage and target examples of the View format.

- Version Used for this tutorial: 1.0.0(-SNAPSHOT)
- Full example project files: Branch gh-pages, folder: [{{ page.example_folder }}]({{ site.github.repository_url }}/tree/gh-pages/{{ page.example_folder }})


## Project setup 

The Example folder provides a ready to go example.
Refer to the [Getting Started](/wsb-webapp/gettingstarted.html) Page for details if you whish to setup your own project



## What is a View file ? 

A View file is simply a file served by the WebApplication, which extension is set to ``.view``.

It contains some Scala Code, which evaluated as an anonymous function must return an object of type ``HTMLNode`` which in turn will be rendered 
by the handler to a String.

### Warning: What is a View File not

The view files are NOT any kind of special language that is parsed.

 
It is just code, the View handler only copies the content into a String with a class instanciation wrapper, and compiles it.

It is thus in the actual version not very safe...but that is not the point for the moment. 

### The HTML API 

Of course, the View file is executed with already bound programming interfaces to produce the ``HTMLNode`` Objects to be rendered.

- The [src/main/webapp/index.view]({{ site.github.repository_url }}/tree/gh-pages/{{ page.webapp_folder }}/index.view) file is a very simple example, that produces an HTML file

~~~~~~~~~ scala 
html {
    
    head {
        title("Hello World!")
    }
    
    body {
        text("Hello World!")
        
    }
    
}
~~~~~~~~~~~~~~~~

- Start the application by running [{{page.example_folder}}/run.sh]({{ site.github.repository_url }}/tree/gh-pages/{{ page.example_folder }}/run.sh)
- Point your browser to [http://localhost:8087/ExampleApp/index.view](http://localhost:8087/ExampleApp/index.view)

### An extended example

As a startup, let's see how generate a list.
Add to the ``body { ... }`` section:

~~~~~~~~~ scala 
ul {
    
    for (i <- 0 until 6) {
        
       li {
        text(s"Hello $i")
       }
        
    }

}
~~~~~~~~~~~~~

You can also use functional programming style list mapping:

~~~~~~~~~ scala 
ul {
    (0 until 6).map { 
        i => 
            li {
                text(s"Map-Style list element: $i")
            }
    }
}
~~~~~~~~~~~~~


## HTML Components

The reference of available components is here: [Components Reference](/wsb-webapp/view/reference.html)


## Binding Language elements

Now that we have seen that our webpage is just code, you can add code to your project sources, and just use it.

- Example source [src/main/webapp/language-binding.view]({{ site.github.repository_url }}/tree/gh-pages/{{ page.webapp_folder }}/language-binding.view)


### Standard programming

It is possible to simply put some code inside a class or an object, and use it as is.
For example, add to a view file, `BEFORE` the HTML view definition:

~~~~~~~~~ scala 

object UListBuilder {

    def apply = {
        
        ul {
            (0 until 6).map { 
                i => 
                    li {
                        text(s"Map-Style list element: $i")
                    }
            }
        }
        
    }

}

~~~~~~~~~~~~~

Now right after (this MUST be at the end of the view file):

~~~~~~~~~ scala 
html {
    
    head {
        title("Language binding example")
    }
    
    body {
        text("Hello World!")
        
        // Object based
        //--------------------
        UListBuilder.createList
            
    }
    
}
~~~~~~~~~~~~~

### Trait Binding

Another option, which is very convienent if you created a set of functions to be reused accros pages, is to use traits:

- Create a trait : [src/main/scala/example/UListBuilderTrait.scala]({{ site.github.repository_url }}/tree/gh-pages/src/main/scala/example/UListBuilderTrait.scala)

    The source code is not a script there, so it must import the correct interfaces, just consult the source code, we won't go into details here

- In the application main, register it as a compile trait:

    Add to src/main
    
    ~~~~~~~~~ scala
    import com.idyria.osi.wsb.webapp.view.WWWView
    
    WWWView.addCompileTrait(classOf[UListBuilderTrait])
    
    ~~~~~~~~~~~~~~~+



