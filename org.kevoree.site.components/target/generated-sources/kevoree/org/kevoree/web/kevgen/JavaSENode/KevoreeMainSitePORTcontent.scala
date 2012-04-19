package org.kevoree.web.kevgen.JavaSENode
import org.kevoree.framework.port._
import scala.{Unit=>void}
import org.kevoree.web._
class KevoreeMainSitePORTcontent(component : KevoreeMainSite) extends org.kevoree.framework.MessagePort with KevoreeRequiredPort {
def getName : String = "content"
def getComponentName : String = component.getName 
def process(o : Object) = {
{this ! o}
}
def getInOut = false
}
