package org.kevoree.web

import collection.immutable.HashMap
import org.kevoree.library.javase.webserver.{URLHandlerScala, KevoreeHttpResponse, KevoreeHttpRequest, AbstractPage}
import org.kevoree.framework.FileNIOHelper
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.api.Bootstraper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 25/03/12
 * Time: 23:04
 */

object DownloadHelper {

  private var bootService: Bootstraper = null

  def setBootService (b: Bootstraper) {
    bootService = b
  }

  private var logger: Logger = LoggerFactory.getLogger(this.getClass)

  //  def getLastVersion = "1.6.0-BETA7"

  /* Stable */
  def getEditorStableJNLP = "download/KevoreeEditorStableJNLP.jnlp"

  def getPlatformStableJNLP = "download/KevoreeRuntimeStableJNLP.jnlp"

  def getEditorLastRelease = "download/KevoreeEditorLastRelease"

  def getRuntimeLastRelease = "download/KevoreeRuntimeLastRelease"

  /* Snapshot */
  def getEditorSnapshotJNLP = "download/KevoreeEditorSnapshotJNLP.jnlp"

  def getPlatformSnapshotJNLP = "download/KevoreeRuntimeSnapshotJNLP.jnlp"

  def getEditorLastSnapshot = "download/KevoreeEditorLastSnapshot"

  def getRuntimeLastSnapshot = "download/KevoreeRuntimeLastSnapshot"


  def getVariables = HashMap(
                              "editorStableJNLP" -> getEditorStableJNLP,
                              "platformStableJNLP" -> getPlatformStableJNLP,
                              "editorSnapshotJNLP" -> getEditorSnapshotJNLP,
                              "platformSnapshotJNLP" -> getPlatformSnapshotJNLP
                            )

  def checkForDownload (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    val handler = new URLHandlerScala()
    val urlPattern = origin.getDictionary.get("urlpattern").toString
    handler.getLastParam(request.getUrl, urlPattern) match {
      case Some(requestDownload) => {
        if (requestDownload == getEditorStableJNLP) {
          response.getHeaders.put("Content-Type", "application/x-java-jnlp-file")
          response.setRawContent(("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + buildEditorStableJNLP).getBytes("UTF-8"))
          true
        } else if (requestDownload == getPlatformStableJNLP) {
          response.getHeaders.put("Content-Type", "application/x-java-jnlp-file")
          response.setRawContent(("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + buildRuntimeStableJNLP).getBytes("UTF-8"))
          true
        } else if (requestDownload == getEditorSnapshotJNLP) {
          response.getHeaders.put("Content-Type", "application/x-java-jnlp-file")
          response.setRawContent(("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + buildEditorSnapshotJNLP).getBytes("UTF-8"))
          true
        } else if (requestDownload == getPlatformSnapshotJNLP) {
          response.getHeaders.put("Content-Type", "application/x-java-jnlp-file")
          response.setRawContent(("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + buildRuntimeSnapshotJNLP).getBytes("UTF-8"))
          true
        } else if (requestDownload == getEditorLastSnapshot) {
          val bytes = getBytesForEditorLastSnapshot
          if (bytes.length > 0) {
            response.getHeaders.put("Content-Type", "application/x-java-archive")
            response.setRawContent(bytes)
            true
          } else {
            logger.warn("Unable to get the last jar for snapshot editor")
            false
          }
        } else if (requestDownload == getRuntimeLastSnapshot) {
          val bytes = getBytesForRuntimeLastSnapshot
          if (bytes.length > 0) {
            response.getHeaders.put("Content-Type", "application/x-java-archive")
            response.setRawContent(bytes)
            true
          } else {
            logger.warn("Unable to get the last jar for snapshot runtime")
            false
          }
        } else if (requestDownload == getEditorLastRelease) {
          val bytes = getBytesForEditorLastRelease
          if (bytes.length > 0) {
            response.getHeaders.put("Content-Type", "application/x-java-archive")
            response.setRawContent(bytes)
            true
          } else {
            logger.warn("Unable to get the last jar for snapshot editor")
            false
          }
        } else if (requestDownload == getRuntimeLastRelease) {
          val bytes = getBytesForRuntimeLastRelease
          if (bytes.length > 0) {
            response.getHeaders.put("Content-Type", "application/x-java-archive")
            response.setRawContent(bytes)
            true
          } else {
            logger.warn("Unable to get the last jar for snapshot runtime")
            false
          }
        } else {
          false
        }
      }
      case None => false
    }
  }


  /*private def getStableVersion: String = {
    AetherUtil.resolveVersion("org.kevoree", "org.kevoree.core", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
  }

  private def getSnapshotVersion: String = {
    AetherUtil.resolveVersion("org.kevoree", "org.kevoree.core", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
  }*/

  def buildEditorStableJNLP: String = {
    //    val stableVersion = "http://maven.kevoree.org/release/org/kevoree/tools/org.kevoree.tools.ui.editor.standalone/" + getStableVersion + "/org.kevoree.tools.ui.editor.standalone-" +
    //    getStableVersion + ".jar"
    val stableVersion = "http://kevoree.org/" + getEditorLastRelease
    //    <?xml version="1.0" encoding="utf-8"?>
    <jnlp spec="1.0" codebase="http://kevoree.org/">
      <information>
        <title>Kevoree Model Editor</title>
        <vendor>IRISA / INRIA Centre Rennes Bretagne Atlantique</vendor>
          <icon href="icon/kev-logo-full.png" width="64" height="64"/>
        <!-- allow app to run without Internet access -->
          <offline-allowed/>
        <shortcut online="false">
            <desktop/>
            <menu submenu="Kevoree"/>
        </shortcut>
          <association mime-type="application-x/kevmodel" extensions="kev"/>
      </information>
      <resources>
          <jar href={stableVersion} main="true"/>
      </resources>
      <application-desc name="KevEditor" main-class="org.kevoree.tools.ui.editor.standalone.App">
          <update check="background" policy="prompt-update"/>
      </application-desc>
      <security>
          <all-permissions/>
      </security>
    </jnlp>.toString()
  }

  def buildRuntimeStableJNLP: String = {
    //    val stableVersion = "http://maven.kevoree.org/release/org/kevoree/platform/org.kevoree.platform.standalone.gui/" + getStableVersion + "/org.kevoree.platform.standalone.gui-" + getStableVersion +
    //      ".jar"
    val stableVersion = "http://kevoree.org/" + getRuntimeLastRelease
    //    <?xml version="1.0" encoding="utf-8"?>
    <jnlp spec="1.0" codebase="http://kevoree.org/">
      <information>
        <title>Kevoree Runtime</title>
        <vendor>IRISA / INRIA Centre Rennes Bretagne Atlantique</vendor>
          <icon href="icon/kev-logo-full.png" width="64" height="64"/>
        <!-- allow app to run without Internet access -->
          <offline-allowed/>
        <shortcut online="false">
          <!-- create desktop shortcut -->
            <desktop/>
            <menu submenu="Kevoree"/>
        </shortcut>
          <association mime-type="application-x/kevmodel" extensions="kev"/>
      </information>
      <resources>
          <jar href={stableVersion} main="true"/>
      </resources>
      <application-desc main-class="org.kevoree.platform.standalone.gui.App">
          <update check="background"/>
      </application-desc>
      <security>
          <all-permissions/>
      </security>
    </jnlp>.toString()
  }

  def buildEditorSnapshotJNLP: String = {
    val stableVersion = "http://kevoree.org/" + getEditorLastSnapshot
    //    <?xml version="1.0" encoding="utf-8"?>
    <jnlp spec="1.0" codebase="http://kevoree.org/">
      <information>
        <title>Kevoree Model Editor</title>
        <vendor>IRISA / INRIA Centre Rennes Bretagne Atlantique</vendor>
          <icon href="icon/kev-logo-full.png" width="64" height="64"/>
        <!-- allow app to run without Internet access -->
          <offline-allowed/>
        <shortcut online="false">
            <desktop/>
            <menu submenu="Kevoree"/>
        </shortcut>
          <association mime-type="application-x/kevmodel" extensions="kev"/>
      </information>
      <resources>
          <jar href={stableVersion} main="true"/>
      </resources>
      <application-desc name="KevEditor" main-class="org.kevoree.tools.ui.editor.standalone.App">
          <update check="background" policy="prompt-update"/>
      </application-desc>
      <security>
          <all-permissions/>
      </security>
    </jnlp>.toString()
  }

  def buildRuntimeSnapshotJNLP: String = {
    val stableVersion = "http://kevoree.org/" + getRuntimeLastSnapshot
    //    <?xml version="1.0" encoding="utf-8"?>
    <jnlp spec="1.0" codebase="http://kevoree.org/">
      <information>
        <title>Kevoree Runtime</title>
        <vendor>IRISA / INRIA Centre Rennes Bretagne Atlantique</vendor>
          <icon href="icon/kev-logo-full.png" width="64" height="64"/>
        <!-- allow app to run without Internet access -->
          <offline-allowed/>

        <shortcut online="false">
          <!-- create desktop shortcut -->
            <desktop/>
            <menu submenu="Kevoree"/>
        </shortcut>
          <association mime-type="application-x/kevmodel" extensions="kev"/>
      </information>
      <resources>
          <jar href={stableVersion} main="true"/>
      </resources>
      <application-desc main-class="org.kevoree.platform.standalone.gui.App">
          <update check="background"/>
      </application-desc>
      <security>
          <all-permissions/>
      </security>
    </jnlp>.toString()
  }

  def getBytesForEditorLastRelease: Array[Byte] = {
    val file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
    FileNIOHelper.getBytesFromFile(file)
  }

  def getBytesForRuntimeLastRelease: Array[Byte] = {
    val file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
    FileNIOHelper.getBytesFromFile(file)
  }

  def getBytesForEditorLastSnapshot: Array[Byte] = {
    val file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
    FileNIOHelper.getBytesFromFile(file)
  }

  def getBytesForRuntimeLastSnapshot: Array[Byte] = {
    val file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
    FileNIOHelper.getBytesFromFile(file)
  }
}
