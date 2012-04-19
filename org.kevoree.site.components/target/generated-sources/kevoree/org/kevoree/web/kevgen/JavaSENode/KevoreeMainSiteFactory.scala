package org.kevoree.web.kevgen.JavaSENode
import org.kevoree.framework._
import org.kevoree.web._
class KevoreeMainSiteFactory extends org.kevoree.framework.osgi.KevoreeInstanceFactory {
override def registerInstance(instanceName : String, nodeName : String)=KevoreeMainSiteFactory.registerInstance(instanceName,nodeName)
override def remove(instanceName : String)=KevoreeMainSiteFactory.remove(instanceName)
def createInstanceActivator = KevoreeMainSiteFactory.createInstanceActivator}
object KevoreeMainSiteFactory extends org.kevoree.framework.osgi.KevoreeInstanceFactory {
def createInstanceActivator: org.kevoree.framework.osgi.KevoreeInstanceActivator = new KevoreeMainSiteActivator
def createComponentActor() : KevoreeComponent = {
new KevoreeComponent(createKevoreeMainSite()){def startComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.web.KevoreeMainSite].startPage()}
def stopComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.web.KevoreeMainSite].stopPage()}
override def updateComponent(){getKevoreeComponentType.asInstanceOf[org.kevoree.web.KevoreeMainSite].updatePage()}
}}
def createKevoreeMainSite() : org.kevoree.web.KevoreeMainSite ={
var newcomponent = new org.kevoree.web.KevoreeMainSite();
newcomponent.getHostedPorts().put("request",createKevoreeMainSitePORTrequest(newcomponent))
newcomponent.getNeededPorts().put("content",createKevoreeMainSitePORTcontent(newcomponent))
newcomponent}
def createKevoreeMainSitePORTrequest(component : KevoreeMainSite) : KevoreeMainSitePORTrequest ={ new KevoreeMainSitePORTrequest(component)}
def createKevoreeMainSitePORTcontent(component : KevoreeMainSite) : KevoreeMainSitePORTcontent ={ return new KevoreeMainSitePORTcontent(component);}
}
