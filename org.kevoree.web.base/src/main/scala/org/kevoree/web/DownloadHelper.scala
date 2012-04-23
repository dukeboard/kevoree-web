package org.kevoree.web

import scala.collection.immutable.HashMap
import org.kevoree.framework.FileNIOHelper
import org.slf4j.{LoggerFactory, Logger}
import scala.actors.Actor
import java.io.File
import scala.Predef._
import org.kevoree.library.javase.webserver._
import org.kevoree.api.Bootstraper
import scala._
import java.util.{TimerTask, Timer}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 25/03/12
 * Time: 23:04
 */

class DownloadHelper (bootService: Bootstraper) extends Actor {

  /*private var bootService: Bootstraper = null

  def setBootService (b: Bootstraper) {
    bootService = b
  }*/

  private var logger: Logger = LoggerFactory.getLogger(this.getClass)

  //  def getLastVersion = "1.6.0-BETA7"

  /* Stable */
  private def getEditorStableJNLP = "download/KevoreeEditor.jnlp"

  //  def getEditorStableJNLP = "http://dist.kevoree.org/KevoreeEditorStable.php"

  private def getPlatformStableJNLP = "download/KevoreeRuntime.jnlp"

  //   def getPlatformStableJNLP = "http://dist.kevoree.org/KevoreeRuntimeStable.php"

  private def getEditorLastRelease = "download/KevoreeEditorLastRelease"

  private def getRuntimeLastRelease = "download/KevoreeRuntimeLastRelease"

  /* Snapshot */
  private def getEditorSnapshotJNLP = "download/KevoreeEditorSnapshot.jnlp"

  //  def getEditorSnapshotJNLP = "http://dist.kevoree.org/KevoreeEditorSnapshot.php"

  private def getPlatformSnapshotJNLP = "download/KevoreeRuntimeSnapshot.jnlp"

  //  def getPlatformSnapshotJNLP = "http://dist.kevoree.org/KevoreeRuntimeSnapshot.php"

  private def getEditorLastSnapshot = "download/KevoreeEditorLastSnapshot"

  private def getRuntimeLastSnapshot = "download/KevoreeRuntimeLastSnapshot"

  def getVariables = HashMap(
                              "editorStableJNLP" -> getEditorStableJNLP,
                              "platformStableJNLP" -> getPlatformStableJNLP,
                              "editorSnapshotJNLP" -> getEditorSnapshotJNLP,
                              "platformSnapshotJNLP" -> getPlatformSnapshotJNLP
                            )

  var editorLastRelease = ""
  var editorLastSnapshot = ""
  var runtimeLastRelease = ""
  var runtimeLastSnapshot = ""

//  var running = true
  var timer: Timer = null


  override def start () = {
    super.start()
    timer = new Timer()
    timer.schedule(new TimerTask {
      def run () {
        try {
        logger.debug("updating maven artifact")
        var file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
        updateEditorLastRelease(file.getAbsolutePath)
        file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
        updateRuntimeLastRelease(file.getAbsolutePath)
        file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
        updateEditorLastSnapshot(file.getAbsolutePath)
        file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
        updateRuntimeLastSnapshot(file.getAbsolutePath)
        logger.debug("maven artifact updated")
        } catch {
          case _@e => logger.debug("Uanble to update maven artifact", e)
        }
      }
    },1, 21600000)
    this
  }

  case class DOWNLOAD (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse)

  case class UPDATE_EDITOR_LAST_RELEASE (filePath: String)

  case class UPDATE_RUNTIME_LAST_RELEASE (filePath: String)

  case class UPDATE_EDITOR_LAST_SNAPSHOT (filePath: String)

  case class UPDATE_RUNTIME_LAST_SNAPSHOT (filePath: String)

  case class STOP ()


  def checkForDownload (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    (this !? DOWNLOAD(index, origin, request, response)).asInstanceOf[Boolean]
  }

  def updateEditorLastRelease (filePath: String) {
    this ! UPDATE_EDITOR_LAST_RELEASE(filePath)
  }

  def updateRuntimeLastRelease (filePath: String) {
    this ! UPDATE_RUNTIME_LAST_RELEASE(filePath)
  }

  def updateEditorLastSnapshot (filePath: String) {
    this ! UPDATE_EDITOR_LAST_SNAPSHOT(filePath)
  }

  def updateRuntimeLastSnapshot (filePath: String) {
    this ! UPDATE_RUNTIME_LAST_SNAPSHOT(filePath)
  }

  def stop () {
    this ! STOP()
  }

  def act () {
    loop {
      react {
        case DOWNLOAD(index, origin, request, response) => reply(checkForDownloadInternals(index, origin, request, response))
        case UPDATE_EDITOR_LAST_RELEASE(filePath) => editorLastRelease = filePath
        case UPDATE_RUNTIME_LAST_RELEASE(filePath) => runtimeLastRelease = filePath
        case UPDATE_EDITOR_LAST_SNAPSHOT(filePath) => editorLastSnapshot = filePath
        case UPDATE_RUNTIME_LAST_SNAPSHOT(filePath) => runtimeLastSnapshot = filePath
        case STOP() => timer.cancel(); timer.purge(); this.exit()
      }
    }
  }

  private def checkForDownloadInternals (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    val handler = new URLHandlerScala()
    val urlPattern = origin.getDictionary.get("urlpattern").toString
    handler.getLastParam(request.getUrl, urlPattern) match {
      case Some(requestDownload) => {
        if (requestDownload == getEditorLastSnapshot) {
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

  private def getBytesForEditorLastRelease: Array[Byte] = {
    //    val file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
    FileNIOHelper.getBytesFromFile(new File(editorLastRelease))
  }

  private def getBytesForRuntimeLastRelease: Array[Byte] = {
    //    val file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "RELEASE", List[String]("http://maven.kevoree.org/release/"))
    FileNIOHelper.getBytesFromFile(new File(runtimeLastRelease))
  }

  private def getBytesForEditorLastSnapshot: Array[Byte] = {
    //    val file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
    FileNIOHelper.getBytesFromFile(new File(editorLastSnapshot))
  }

  private def getBytesForRuntimeLastSnapshot: Array[Byte] = {
    //    val file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
    FileNIOHelper.getBytesFromFile(new File(runtimeLastSnapshot))
  }

}
