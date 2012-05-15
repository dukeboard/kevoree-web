package org.kevoree.web

import scala.collection.immutable.HashMap
import org.kevoree.framework.FileNIOHelper
import org.slf4j.{LoggerFactory, Logger}
import scala.actors.Actor
import scala.Predef._
import org.kevoree.library.javase.webserver._
import org.kevoree.api.Bootstraper
import scala._
import java.util.jar.{JarEntry, JarFile}
import java.util.{Random, TimerTask, Timer}
import java.io.{ByteArrayOutputStream, InputStream, File}
import util.matching.Regex

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 25/03/12
 * Time: 23:04
 */

class DownloadHelper (bootService: Bootstraper) extends Actor {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)


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

  var variables = HashMap(
                           "editorStableJNLP" -> getEditorStableJNLP,
                           "platformStableJNLP" -> getPlatformStableJNLP,
                           "editorSnapshotJNLP" -> getEditorSnapshotJNLP,
                           "platformSnapshotJNLP" -> getPlatformSnapshotJNLP,
                           "kevoree.version.release" -> "toto",
                           "kevoree.version.snapshot" -> "titi"
                         )

  def getVariables = variables

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
          // using latest version on release repository to get the a.b.c instead of a.b.c-z like we get with RELEASE version
          var file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", List[String]("http://maven.kevoree.org/release/"))
          updateEditorLastRelease(file.getAbsolutePath)
          file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/release/"))
          updateRuntimeLastRelease(file.getAbsolutePath)
          file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
          updateEditorLastSnapshot(file.getAbsolutePath)
          file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
          updateRuntimeLastSnapshot(file.getAbsolutePath)
          logger.debug("maven artifact updated")
          logger.debug("update kevoree version values")
          file = bootService.resolveArtifact("org.kevoree.library.model.javase", "org.kevoree.corelibrary.model", "LATEST", List[String]("http://maven.kevoree.org/release/"))
          var jar: JarFile = new JarFile(file)
          var entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
          if (entry != null) {
            updateReleaseVersion(findVersionFromModel(convertStreamToString(jar.getInputStream(entry))))
          }
          file = bootService.resolveArtifact("org.kevoree.library.model.javase", "org.kevoree.corelibrary.model", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
          jar = new JarFile(file)
          entry = jar.getJarEntry("KEV-INF/lib.kev")
          if (entry != null) {
            updateSnapshotVersion(findVersionFromModel(convertStreamToString(jar.getInputStream(entry))))
          }
          logger.debug("kevoree version values updated")
        } catch {
          case _@e => logger.debug("Uanble to update maven artifact", e)
        }
      }
    }, 1, 21600000)
    this
  }

  case class DOWNLOAD (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse)

  case class UPDATE_EDITOR_LAST_RELEASE (filePath: String)

  case class UPDATE_RUNTIME_LAST_RELEASE (filePath: String)

  case class UPDATE_EDITOR_LAST_SNAPSHOT (filePath: String)

  case class UPDATE_RUNTIME_LAST_SNAPSHOT (filePath: String)

  case class UPDATE_RELEASE_VERSION (version: String)

  case class UPDATE_SNAPSHOT_VERSION (version: String)

  case class GET_RELEASE_VERSION ()

  case class GET_SNAPSHOT_VERSION ()

  case class STOP ()

  /*private def getReleaseVersion: String = (this !? GET_RELEASE_VERSION()).asInstanceOf[String]

  private def getSnapshotVersion: String = (this !? GET_SNAPSHOT_VERSION()).asInstanceOf[String]
*/

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

  def updateReleaseVersion (version: String) {
    this ! UPDATE_RELEASE_VERSION(version)
  }

  def updateSnapshotVersion (version: String) {
    this ! UPDATE_SNAPSHOT_VERSION(version)
  }

  def stop () {
    this ! STOP()
  }

  def act () {
    loop {
      react {
        /*case GET_RELEASE_VERSION() => reply(releaseVersion)
        case GET_SNAPSHOT_VERSION() => reply(snapshotVersion)*/
        case DOWNLOAD(index, origin, request, response) => reply(checkForDownloadInternals(index, origin, request, response))
        case UPDATE_EDITOR_LAST_RELEASE(filePath) => editorLastRelease = filePath
        case UPDATE_RUNTIME_LAST_RELEASE(filePath) => runtimeLastRelease = filePath
        case UPDATE_EDITOR_LAST_SNAPSHOT(filePath) => editorLastSnapshot = filePath
        case UPDATE_RUNTIME_LAST_SNAPSHOT(filePath) => runtimeLastSnapshot = filePath
        case UPDATE_RELEASE_VERSION(version) => variables = variables.filterNot(v => v._1 == "kevoree.version.release") + ("kevoree.version.release" -> version)
        case UPDATE_SNAPSHOT_VERSION(version) => variables = variables.filterNot(v => v._1 == "kevoree.version.snapshot") + ("kevoree.version.snapshot" -> version)
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

  private def convertStreamToString (inputStream: InputStream): String = {
    val rand: Random = new Random
    val temp: File = File.createTempFile("kevoreeloaderLib" + rand.nextInt, ".xmi")
    temp.deleteOnExit()
    val out = new ByteArrayOutputStream()
    var read: Int = 0
    val bytes: Array[Byte] = new Array[Byte](1024)
    while ((({
      read = inputStream.read(bytes);
      read
    })) != -1) {
      out.write(bytes, 0, read)
    }
    inputStream.close()
    new String(out.toByteArray, "UTF-8")
  }

  private def findVersionFromModel (path: String): String = {
    //<deployUnits type="jar" unitName="org.kevoree.framework" xsi:type="kevoree:DeployUnit" groupName="org.kevoree" version="1.7.1-SNAPSHOT" targetNodeType="//@typeDefinitions.17"></deployUnits>
    val frameworkRegex = new
      //Regex("<deployUnits targetNodeType=\"//@typeDefinitions.[0-9][0-9]*\" type=\".*\" version=\"(.*)\" unitName=\"org.kevoree.framework\" groupName=\"org.kevoree\" xsi:type=\"kevoree:DeployUnit\"></deployUnits>")
        Regex("<deployUnits\\s(?:(?:(?:version=\"([^\\s]*)\")|(?:targetNodeType=\"//@typeDefinitions.[0-9][0-9]*\")|(?:type=\"[^\\s]*\")|(?:unitName=\"org.kevoree.framework\")|(?:groupName=\"org.kevoree\")|(?:xsi:type=\"kevoree:DeployUnit\"))(\\s)*){6}></deployUnits>")
    var frameworkVersion = "LATEST"
    path.lines.forall(l => {
      val m = frameworkRegex.pattern.matcher(l)
      if (m.find()) {
        frameworkVersion = m.group(1)
        false
      } else {
        true
      }
    })
    frameworkVersion
  }

}
