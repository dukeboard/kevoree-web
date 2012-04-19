package org.kevoree.web.kevgen.JavaSENode
import org.kevoree.framework._
import org.kevoree.web._
class KevoreeMainSiteDevFactory extends org.kevoree.framework.osgi.KevoreeInstanceFactory {
override def registerInstance(instanceName : String, nodeName : String)=KevoreeMainSiteDevFactory.registerInstance(instanceName,nodeName)
override def remove(instanceName : String)=KevoreeMainSiteDevFactory.remove(instanceName)
def createInstanceActivator = KevoreeMainSiteDevFactory.createInstanceActivator}
object KevoreeMainSiteDevFactory extends org.kevoree.framework.osgi.KevoreeInstanceFactory {
def createInstanceActivator: org.kevoree.framework.osgi.KevoreeInstanceActivator = new KevoreeMainSiteDevActivator
def createComponentActor() : KevoreeComponent = {
new KevoreeComponent(createKevoreeMainSiteDev()){def startComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.web.KevoreeMainSiteDev].startPage()}
def stopComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.web.KevoreeMainSiteDev].stopPage()}
override def updateComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.web.KevoreeMainSiteDev].updatePage()}
}}
def createKevoreeMainSiteDev() : org.kevoree.web.KevoreeMainSiteDev ={
var newcomponent = new org.kevoree.web.KevoreeMainSiteDev();
newcomponent.getHostedPorts().put("request",createKevoreeMainSiteDevPORTrequest(newcomponent))
newcomponent.getNeededPorts().put("content",createKevoreeMainSiteDevPORTcontent(newcomponent))
newcomponent}
def createKevoreeMainSiteDevPORTrequest(component : KevoreeMainSiteDev) : KevoreeMainSiteDevPORTrequest ={ new KevoreeMainSiteDevPORTrequest(component)}
def createKevoreeMainSiteDevPORTcontent(component : KevoreeMainSiteDev) : KevoreeMainSiteDevPORTcontent ={ return new KevoreeMainSiteDevPORTcontent(component);}
}
