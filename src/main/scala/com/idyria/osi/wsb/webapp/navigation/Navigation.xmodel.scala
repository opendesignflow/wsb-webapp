package com.idyria.osi.wsb.webapp.navigation

import com.idyria.osi.ooxoo.model.out.scala._
import com.idyria.osi.ooxoo.model.out.scala._
import com.idyria.osi.ooxoo.model.out.markdown._
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer


@producers(Array(
    new producer(value=classOf[ScalaProducer]),
     new producer(value=classOf[MDProducer])
)) 
object NavigationModelBuilder extends ModelBuilder {  

   // parameter("scalaProducer.targetPackage","com.idyria.osi.wsb.webapp.navigation")

    // Trait to Describe elements
    //---------
    "Info" is {
        isTrait
        
        attribute("name")
        "Description" is "string"
    
    }
    


    // Main group Definition
    //---------------
    "GroupTrait" multiple {
        
        isTrait
        withTrait("Info")
        withTrait("PathTrait")

        // Id For this subpath
        attribute("id")
         
        // A Possible view to associate this Group with
        attribute("view")
        
        // Rules
        //--------------
        "Rule" multiple {
            
            withTrait("Info")
            withTrait("PathTrait")
            attribute("id")
            attribute("for") ofType("regexp") and {
                "Defines the regexp filter for which this Rule will apply."
                
            } 
            attribute("failTo")
            attribute("successTo")
            
            //-- parameters
            "Parameters" ofType("map")
            /*"Parameter" multiple {
              attribute("name")
              "Value" ofType "cdata"
            }*/
        
        }   
        
        // Views
        //----------
        "View" multiple {
            
            withTrait("Info")
            withTrait("PathTrait")
            attribute("id")
        }
     
        "Group" multiple {
            
            //classType("DefaultGroup")
             withTrait("GroupTrait")
            
         }
    }

    // Base Navigation Trait
    //----------
    "Navigation" is {
        
        withTrait("GroupTrait")
        
        
    
    
    
    
    }


}
