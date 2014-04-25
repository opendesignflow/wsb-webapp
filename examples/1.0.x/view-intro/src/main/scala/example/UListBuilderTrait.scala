package example

import com.idyria.osi.wsb.webapp.view.WebappHTMLBuilder

trait UListBuilderTrait extends WebappHTMLBuilder {
  
  
  def createList = {
        
        ul {
            (0 until 6).map { 
                i => 
                    li {
                        text(s"Trait Builder list element: $i")
                    }
            }
        }
        
    }
}
