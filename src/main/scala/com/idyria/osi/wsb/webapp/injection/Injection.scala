package com.idyria.osi.wsb.webapp.injection

import scala.collection.JavaConversions
import com.idyria.osi.wsb.webapp.view.Inject
import scala.reflect.runtime.universe._
import scala.reflect._
import com.idyria.osi.tea.logging.TLogSource

/**
 * Trait to be implemented by classes that could provide injection support for certain data types
 */
trait Injector {

  /**
   * To be implemented by Injection supporting classes to indicate the supported classes
   */
  def supportedTypes: List[Class[_]]

  /**
   * Called by Injector singleton to ask this class to try to provide a value for this field
   */
  def inject[T](id: String, dataType: Class[T]): Option[T]

}

class AnyIDInjector(val target : AnyRef) extends Injector {
  
 // this.supportedTypes = this.supportedTypes +: target.getClass
  
  def supportedTypes = List(target.getClass)
  
   def inject[T](id: String, dataType: Class[T]): Option[T] = {
    Some(target.asInstanceOf[T])
  }
  
}

object Injector extends TLogSource {

  /**
   * Implicit Class used by string context for string interpolation injection:
   *
   * var value : Type = inject"id"
   *
   */
  implicit class InjectionHelper(val sc: StringContext) extends AnyVal {

    /*def inject[T](args: Class[T]*): T = {

      sys.error(s"TODO - IMPLEMENT for type: " + args + " ")

      Injector.inject(sc.parts(0).toString) match {
        case Some(value) ⇒ value
        case None        ⇒ throw new RuntimeException(s"Used inject string interpolation with id ${sc.parts(0)} yield None result ")
      }

      //var res: T = _
      //res
    }*/
  }

  var supportedTypes = Map[Class[_], List[Injector]]()

  def clear = {
    supportedTypes = Map[Class[_], List[Injector]]()
  }
  
  /**
   * Record an injector in local types support
   */
  def apply(injector: Injector) = {

    injector.supportedTypes.foreach {

      t ⇒ supportedTypes = supportedTypes + (t -> (supportedTypes.getOrElse(t, List[Injector]()) :+ injector))
    }

  }
  
  /**
   * Record an object to be injectable by any ID or a specific ID if it is an injectable type
   */
  def apply(obj:AnyRef) = {
    
    logFine[Injector]("Making available type : " + obj.getClass)
    
    this.supportedTypes = supportedTypes + (obj.getClass -> List(new AnyIDInjector(obj)))
    
  }

  /**
   * Return an object or null matching the provided id
   */
  def inject[T](id: String)(implicit tag: ClassTag[T]): T = {

    Injector.injectOption[T](id)(tag) match {
      case None ⇒ throw new RuntimeException(s"Injecting value for id $id did not yield any result")
      case Some(value) ⇒ value.asInstanceOf[T]
    }

  }

  def injectOption[T](id: String)(implicit tag: ClassTag[T]): Option[T] = {

    // get class
    var cl = tag.runtimeClass.asInstanceOf[Class[T]]

    // Look in all supported types for and injector having our id

    var resObjects = supportedTypes.filter(t ⇒ t._1 == cl).map { case (cl, injectors) ⇒ injectors.map(_.inject(id, cl)).filterNot(_ == None) }.flatten

    /*var resObjects = supportedTypes.map {
      case (cl, injectors) ⇒ injectors.map { _.inject(id, cl) }.filterNot(_ == None)
    }.flatten*/

    // Analyse results
    //---------
    resObjects.size match {
      case 0 ⇒ None
      case 1 ⇒ resObjects.head.asInstanceOf[Option[T]]
      case s ⇒ throw new RuntimeException(s"Used id only ($id) Injection without type definition yield multiple possible values of classes: ${resObjects.map { _.get.getClass() }}, change injection method or make the ids unique")
    }

  }

  /**
   * Gets all the fields of the target Object, and for each tries to find a set of injectors and ask them to provide a value
   *
   * FIXME: If multiple injectors can provide a value, fail as conflict
   */
  def inject(targetObject: Any) = {

    //println("Doing Injection for: " + targetObject)
    var currentClass = targetObject.getClass()
    var classes = List[Class[_]]()
    while(currentClass!=null) {
      classes = classes :+ currentClass
      currentClass = currentClass.getSuperclass
    }
    
    // Map classes to fields and flatten to go on
    classes.map(cl => cl.getDeclaredFields().toList ::: cl.getFields().toList ).flatten.filter {

      // Filter unsupported types and ones without inject annotation
      field ⇒

        // println("Testing field: " + field.getName() + " -> " + field.getAnnotation(classOf[Inject]))
        
      logFine[Injector]("Testing field: " +  field.getName() + s" (${field.getType.getCanonicalName}) -> " + field.getAnnotation(classOf[Inject]))
      
        var res = field.getAnnotation(classOf[Inject]) != null && supportedTypes.find{case (c,inj) => field.getType.isAssignableFrom(c) }!=None
        res
    }.foreach {

      // For each supported
      field ⇒

        // Find Id through inject annotation
        var id = field.getAnnotation(classOf[Inject]).value()

        logFine[Injector]("Supported field: " + field.getName() + " with id " + id)

        // Find Values
         var values = supportedTypes.collectFirst{case (c,inj) if( field.getType.isAssignableFrom(c) ) => inj}.get.map(_.inject(id, field.getType())).filterNot(_ == None)
       // var values = supportedTypes(field.getType()).map(_.inject(id, field.getType())).filterNot(_ == None)

        // Error if multiple values
        values.size match {

          // Null if no value
          case 0 ⇒
            field.setAccessible(true);

            field.set(targetObject, null)
          case 1 ⇒

            //println("Setting value  " + values.head)
          logFine[Injector]("Setting value  " + values.head)
            field.setAccessible(true); field.set(targetObject, values.head.get)
          case _ ⇒ 
          
          throw new RuntimeException(s"Injection on field ${field.getName()} (id: $id) of class ${targetObject.getClass} failed because multiple values are available")

        }

    }

  }

}