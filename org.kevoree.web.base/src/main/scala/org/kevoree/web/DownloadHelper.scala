package org.kevoree.web

import org.kevoree.framework.{KevoreeXmiHelper, FileNIOHelper}
import scala.actors.Actor
import scala.Predef._
import org.kevoree.library.javase.webserver._
import org.kevoree.api.Bootstraper
import scala._
import java.util.jar.{JarEntry, JarFile}
import java.io.{InputStream, File}
import java.text.SimpleDateFormat
import java.util

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 25/03/12
 * Time: 23:04
 */

object DownloadHelper {
  var downloadHelper = new DownloadHelper(null, null)

  def getVariables = downloadHelper.getVariables
}

class DownloadHelper(bootService: Bootstraper, mainSite: KevoreeMainSite) extends Actor {

  DownloadHelper.downloadHelper = this

  /* Stable */
//  private def getEditorStableJNLP = "download/KevoreeEditor.jnlp"

  private def getEditorStableJAR = "download/KevoreeEditor.jar"

  //  def getEditorStableJNLP = "http://dist.kevoree.org/KevoreeEditorStable.php"

//  private def getPlatformStableJNLP = "download/KevoreeRuntime.jnlp"

  private def getPlatformStableGUIJAR = "download/KevoreeRuntimeGUI.jar"

  private def getPlatformStableJAR = "download/KevoreeRuntime.jar"

  //   def getPlatformStableJNLP = "http://dist.kevoree.org/KevoreeRuntimeStable.php"

  private def getEditorLastRelease = "download/KevoreeEditorLastRelease.jar"

  private def getRuntimeLastRelease = "download/KevoreeRuntimeLastRelease.jar"

  /* Snapshot */
//  private def getEditorSnapshotJNLP = "download/KevoreeEditorSnapshot.jnlp"

  private def getEditorSnapshotJAR = "download/KevoreeEditorSnapshot.jar"

  //  def getEditorSnapshotJNLP = "http://dist.kevoree.org/KevoreeEditorSnapshot.php"

//  private def getPlatformSnapshotJNLP = "download/KevoreeRuntimeSnapshot.jnlp"

  private def getPlatformSnapshotGUIJAR = "download/KevoreeRuntimeSnapshotGUI.jar"

  private def getPlatformSnapshotJAR = "download/KevoreeRuntimeSnapshot.jar"

  //  def getPlatformSnapshotJNLP = "http://dist.kevoree.org/KevoreeRuntimeSnapshot.php"

  private def getEditorLastSnapshot = "download/KevoreeEditorLastSnapshot.jar"

  private def getRuntimeLastSnapshot = "download/KevoreeRuntimeLastSnapshot.jar"

  private def getAndroidStableAPK = "download/KevoreeRuntimeAndroidRelease.apk"

  private def getAndroidSnapshotAPK = "download/KevoreeRuntimeAndroidSnapshot.apk"

  private def getSampleRelease = "download/sample.zip"

  // according to our nginx configuration
  private def getUbuntuVM = "download/vms/Ubuntu1204.ova"

  private def getBSDVM = "download/vms/virtualbsd.tar.gz"

  private def getAndroidVM = "download/vms/android4kevoree.ova"


  var variables = Map[String, String](
    /*"editorStableJNLP" -> getEditorStableJNLP,
    "platformStableJNLP" -> getPlatformStableJNLP,
    "editorSnapshotJNLP" -> getEditorSnapshotJNLP,
    "platformSnapshotJNLP" -> getPlatformSnapshotJNLP,*/
    "editorStableJAR" -> getEditorStableJAR,
    "platformStableJAR" -> getPlatformStableJAR,
    "platformStableGUIJAR" -> getPlatformStableGUIJAR,
    "editorSnapshotJAR" -> getEditorSnapshotJAR,
    "platformSnapshotJAR" -> getPlatformSnapshotJAR,
    "platformSnapshotGUIJAR" -> getPlatformSnapshotGUIJAR,
    "androidStableAPK" -> getAndroidStableAPK,
    "androidSnapshotAPK" -> getAndroidSnapshotAPK,
    "kevoree.version.release" -> "RELEASE",
    "kevoree.version.snapshot" -> "LATEST",
    "sampleRelease" -> getSampleRelease,
    "ubuntu.vm" -> getUbuntuVM,
    "bsd.vm" -> getBSDVM,
    "android.vm" -> getAndroidVM
  )

  def getVariables = variables

  /* Be carefull ids are related to the order on variables */
  val editorReleaseFileId = 0
  val editorSnapshotFileId = 1
  val runtimeReleaseGUIFileId = 2
  val runtimeReleaseFileId = 3
  val runtimeSnapshotGUIFileId = 4
  val runtimeSnapshotFileId = 5
  val androidReleaseFileId = 6
  val androidSnapshotFileId = 7
  val sampleFileId = 8

  val listRelease = new util.ArrayList[String](1)
  val listSnapshot = new util.ArrayList[String](1)
  listRelease.add("http://maven.kevoree.org/release/")
  listSnapshot.add("http://maven.kevoree.org/snapshots/")


  var files: Map[Int, String] = Map[Int, String](editorReleaseFileId -> "", editorSnapshotFileId -> "", runtimeReleaseFileId -> "", runtimeSnapshotFileId -> "", androidReleaseFileId -> "",
    androidSnapshotFileId -> "", sampleFileId -> "", runtimeReleaseGUIFileId -> "", runtimeSnapshotGUIFileId -> "")

  //  var running = true
  var timer: util.Timer = null

  private def update(id: Int) {
    var file: File = null
    id match {
      case i if (id == editorReleaseFileId) => {
        org.kevoree.log.Log.debug("Try to update Editor Release")
        // using latest version on release repository to get the a.b.c instead of a.b.c-z like we get with RELEASE version
        file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", listRelease)
      }
      case i if (id == editorSnapshotFileId) => {
        org.kevoree.log.Log.debug("Try to update Editor Snapshot")
        file = bootService.resolveArtifact("org.kevoree.tools.ui.editor.standalone", "org.kevoree.tools", "LATEST", listSnapshot)
      }
      case i if (id == runtimeReleaseGUIFileId) => {
        org.kevoree.log.Log.debug("Try to update runtime Gui Release")
        file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", listRelease)
      }
      case i if (id == runtimeSnapshotGUIFileId) => {
        org.kevoree.log.Log.debug("Try to update runtime GUI Snapshot")
        file = bootService.resolveArtifact("org.kevoree.platform.standalone.gui", "org.kevoree.platform", "LATEST", listSnapshot)
      }
      case i if (id == runtimeReleaseFileId) => {
        org.kevoree.log.Log.debug("Try to update runtime Release")
        file = bootService.resolveArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", "LATEST", listRelease)
      }
      case i if (id == runtimeSnapshotFileId) => {
        org.kevoree.log.Log.debug("Try to update runtime Snapshot")
        file = bootService.resolveArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", "LATEST", listSnapshot)
      }
      case i if (id == androidReleaseFileId) => {
        org.kevoree.log.Log.debug("Try to update Android Release")
        file = bootService.resolveArtifact("org.kevoree.platform.android.apk", "org.kevoree.platform", "LATEST", "apk", listRelease)
      }
      case i if (id == androidSnapshotFileId) => {
        org.kevoree.log.Log.debug("Try to update Android Snapshot")
        file = bootService.resolveArtifact("org.kevoree.platform.android.apk", "org.kevoree.platform", "LATEST", "apk", listSnapshot)
      }
      case i if (id == sampleFileId) => {
        org.kevoree.log.Log.debug("Try to update sample Release")
        file = bootService.resolveArtifact("org.kevoree.library.sample.javase.root", "org.kevoree.corelibrary.sample", "LATEST", "zip", listRelease)
      }
      case _ =>
    }
    if (file != null) {
      files = files.filterKeys(i => i != id) ++ Map[Int, String](id -> file.getAbsolutePath)
    }
  }


  override def start() = {
    super.start()
    timer = new util.Timer()
    timer.schedule(new util.TimerTask {
      def run() {
        try {
          org.kevoree.log.Log.debug("updating maven artifact")
          updateFile(editorReleaseFileId)
          updateFile(runtimeReleaseGUIFileId)
          updateFile(runtimeReleaseFileId)
          updateFile(androidReleaseFileId)
          updateFile(editorSnapshotFileId)
          updateFile(runtimeSnapshotGUIFileId)
          updateFile(runtimeSnapshotFileId)
          updateFile(androidSnapshotFileId)
          updateFile(sampleFileId)
          org.kevoree.log.Log.debug("maven artifact updated")

          org.kevoree.log.Log.debug("update kevoree version values")
          var file = bootService.resolveArtifact("org.kevoree.library.model.javase", "org.kevoree.corelibrary.model", "LATEST", listRelease)
          var jar: JarFile = new JarFile(file)
          var entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
          if (entry != null) {
            updateReleaseVersion(findVersionFromModel(/*convertStreamToString(*/ jar.getInputStream(entry) /*)*/))
          }
          file = bootService.resolveArtifact("org.kevoree.library.model.javase", "org.kevoree.corelibrary.model", "LATEST", listSnapshot)
          jar = new JarFile(file)
          entry = jar.getJarEntry("KEV-INF/lib.kev")
          if (entry != null) {
            updateSnapshotVersion(findVersionFromModel(/*convertStreamToString(*/ jar.getInputStream(entry) /*)*/))
          }
          org.kevoree.log.Log.debug("kevoree version values updated")
        } catch {
          case _@e =>  org.kevoree.log.Log.debug("Unable to update maven artifact", e)
        }
      }
    }, 1, /*21*/ 1800000)
    this
  }

  case class DOWNLOAD(index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse)

  case class UPDATE /*_FILE*/ (/*filePath: String, */ fileId: Int)

  case class UPDATE_RELEASE_VERSION(version: String)

  case class UPDATE_SNAPSHOT_VERSION(version: String)

  case class STOP()

  def checkForDownload(index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    (this !? DOWNLOAD(index, origin, request, response)).asInstanceOf[Boolean]
  }

  def updateFile(/*filePath: String, */ id: Int) {
    this !? UPDATE /*_FILE*/ (/*filePath, */ id)
  }

  def updateReleaseVersion(version: String) {
    this ! UPDATE_RELEASE_VERSION(version)
  }

  def updateSnapshotVersion(version: String) {
    this ! UPDATE_SNAPSHOT_VERSION(version)
  }

  def stop() {
    this ! STOP()
  }

  def act() {
    loop {
      react {
        case DOWNLOAD(index, origin, request, response) => reply(checkForDownloadInternals(index, origin, request, response))
        //        case UPDATE_FILE(filePath, id) => files = files.filterKeys(i => i != id) ++ Map[Int, String](id -> filePath)
        case UPDATE(id) => update(id); reply(true)
        case UPDATE_RELEASE_VERSION(version) => {
          variables = variables.filterKeys(i => i != "kevoree.version.release") + ("kevoree.version.release" -> version)
          var pattern: String = mainSite.getDictionary.get("urlpattern").toString
          if (pattern.endsWith("**")) {
            pattern = pattern.replace("**", "")
          }
          if (!pattern.endsWith("/")) {
            pattern = pattern + "/"
          }
          org.kevoree.log.Log.debug("invalidate download page: {}", pattern + "download")
          mainSite.invalidateCacheResponse(pattern + "download")
        }
        case UPDATE_SNAPSHOT_VERSION(version) => {
          variables = variables.filterKeys(i => i != "kevoree.version.snapshot") + ("kevoree.version.snapshot" -> version)
          var pattern: String = mainSite.getDictionary.get("urlpattern").toString
          if (pattern.endsWith("**")) {
            pattern = pattern.replace("**", "")
          }
          if (!pattern.endsWith("/")) {
            pattern = pattern + "/"
          }
          org.kevoree.log.Log.debug("invalidate download page: {}", pattern + "download")
          mainSite.invalidateCacheResponse(pattern + "download")
        }
        case STOP() => timer.cancel(); timer.purge(); this.exit()
      }
    }
  }

  private def setLastModifiedHeader(response: KevoreeHttpResponse, fileId: Int) {
    val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", util.Locale.ENGLISH)
    val cal = util.Calendar.getInstance(util.TimeZone.getTimeZone("GMT"))
    format.setCalendar(cal)
    val lastUpdated = format.format(new util.Date(new File(files(fileId)).lastModified()))

    response.getHeaders.put("Last-Modified", lastUpdated)
  }

  private def buildResponse(response: KevoreeHttpResponse, fileId: Int): Boolean = {
    val bytes = getBytesForFile(fileId)
    if (bytes.length > 0) {
      setLastModifiedHeader(response, fileId)
      // must be define if we use tiny webserver
      //      response.getHeaders.put("Content-Length", "" + bytes.length)
      response.setRawContent(bytes)
      true
    } else {
      org.kevoree.log.Log.warn("Unable to get the last jar for snapshot editor")
      false
    }
  }

  private def checkForDownloadInternals(index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    val handler = new URLHandler()
    val urlPattern = origin.getDictionary.get("urlpattern").toString
    handler.getLastParam(request.getUrl, urlPattern) match {
      case null => false
      case requestDownload: String if (requestDownload == getEditorLastSnapshot || requestDownload == "/" + getEditorLastSnapshot) => {
        org.kevoree.log.Log.debug("request to download {}", getEditorLastSnapshot)
        update(editorSnapshotFileId)
        buildResponse(response, editorSnapshotFileId)
      }
      case requestDownload: String if (requestDownload == getRuntimeLastSnapshot || requestDownload == "/" + getRuntimeLastSnapshot) => {
        org.kevoree.log.Log.debug("request to download {}", getRuntimeLastSnapshot)
        update(runtimeSnapshotGUIFileId)
        buildResponse(response, runtimeSnapshotGUIFileId)
      }
      case requestDownload: String if (requestDownload == getEditorLastRelease || requestDownload == "/" + getEditorLastRelease) => {
        org.kevoree.log.Log.debug("request to download {}", getEditorLastRelease)
        buildResponse(response, editorReleaseFileId)
      }
      case requestDownload: String if (requestDownload == getRuntimeLastRelease || requestDownload == "/" + getRuntimeLastRelease) => {
        org.kevoree.log.Log.debug("request to download {}", getRuntimeLastRelease)
        buildResponse(response, runtimeReleaseGUIFileId)
      }
      case requestDownload: String if (requestDownload == getEditorSnapshotJAR || requestDownload == "/" + getEditorSnapshotJAR) => {
        org.kevoree.log.Log.debug("request to download {}", getEditorSnapshotJAR)
        update(editorSnapshotFileId)
        if (buildResponse(response, editorSnapshotFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeEditor-" + getVariables("kevoree.version.snapshot") + ".jar; filename*=utf-8''KevoreeEditor-" +
            getVariables("kevoree.version.snapshot") + ".jar")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getPlatformSnapshotGUIJAR || requestDownload == "/" + getPlatformSnapshotGUIJAR) => {
        org.kevoree.log.Log.debug("request to download {}", getPlatformSnapshotGUIJAR)
        update(runtimeSnapshotGUIFileId)
        if (buildResponse(response, runtimeSnapshotGUIFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-GUI-" + getVariables("kevoree.version.snapshot") + ".jar; filename*=utf-8''KevoreeRuntime-GUI-" +
            getVariables("kevoree.version.snapshot") + ".jar")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getPlatformSnapshotJAR || requestDownload == "/" + getPlatformSnapshotJAR) => {
        org.kevoree.log.Log.debug("request to download {}", getPlatformSnapshotJAR)
        update(runtimeSnapshotFileId)
        if (buildResponse(response, runtimeSnapshotFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.snapshot") + ".jar; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.snapshot") + ".jar")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getEditorStableJAR || requestDownload == "/" + getEditorStableJAR) => {
        org.kevoree.log.Log.debug("request to download {}", getEditorStableJAR)
        if (buildResponse(response, editorReleaseFileId)) {
          response.getHeaders.put("Content-Disposition",
            "attachment; filename=KevoreeEditor-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeEditor-" + getVariables("kevoree.version.release") +
              ".jar")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getPlatformStableJAR || requestDownload == "/" + getPlatformStableJAR) => {
        org.kevoree.log.Log.debug("request to download {}", getPlatformStableJAR)
        if (buildResponse(response, runtimeReleaseFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.release") + ".jar")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getPlatformStableGUIJAR || requestDownload == "/" + getPlatformStableGUIJAR) => {
        org.kevoree.log.Log.debug("request to download {}", getPlatformStableGUIJAR)
        if (buildResponse(response, runtimeReleaseGUIFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-GUI-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeRuntime-GUI-" +
            getVariables("kevoree.version.release") + ".jar")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getAndroidStableAPK || requestDownload == "/" + getAndroidStableAPK) => {
        org.kevoree.log.Log.debug("request to download {}", getAndroidStableAPK)
        if (buildResponse(response, androidReleaseFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.release") + ".apk; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.release") + ".apk")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getAndroidSnapshotAPK || requestDownload == "/" + getAndroidSnapshotAPK) => {
        org.kevoree.log.Log.debug("request to download {}", getAndroidSnapshotAPK)
        update(androidSnapshotFileId)
        if (buildResponse(response, androidSnapshotFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.snapshot") + ".apk; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.release") + ".apk")
          true
        } else {
          false
        }
      }
      case requestDownload: String if (requestDownload == getSampleRelease || requestDownload == "/" + getSampleRelease) => {
        org.kevoree.log.Log.debug("request to download {}", getSampleRelease)
        if (buildResponse(response, sampleFileId)) {
          response.getHeaders.put("Content-Disposition", "attachment; filename=kevoreeSample-" + getVariables("kevoree.version.snapshot") + ".zip; filename*=utf-8''kevoreeSample-" +
            getVariables("kevoree.version.release") + ".zip")
          true
        } else {
          false
        }
      }
      case _ => false
    }
  }

  private def getBytesForFile(id: Int): Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(files(id)))
  }

  private def findVersionFromModel(stream: InputStream): String = {
    val model = KevoreeXmiHelper.instance$.loadStream(stream)

    import scala.collection.JavaConversions._
    model.getDeployUnits.find(dp => dp.getGroupName == "org.kevoree" && dp.getUnitName == "org.kevoree.framework") match {
      case None => "LATEST"
      case Some(deployUnit) => deployUnit.getVersion
    }
  }

}
