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
import java.io.{ByteArrayOutputStream, InputStream, File}
import util.matching.Regex
import java.text.{DateFormat, SimpleDateFormat}
import java.util.{TimeZone, Calendar, TimerTask, Timer, Date, Locale, Random}

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

class DownloadHelper (bootService: Bootstraper, mainSite: KevoreeMainSite) extends Actor {

  DownloadHelper.downloadHelper = this

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)


  /* Stable */
  private def getEditorStableJNLP = "download/KevoreeEditor.jnlp"

  private def getEditorStableJAR = "download/KevoreeEditor.jar"

  //  def getEditorStableJNLP = "http://dist.kevoree.org/KevoreeEditorStable.php"

  private def getPlatformStableJNLP = "download/KevoreeRuntime.jnlp"

  private def getPlatformStableJAR = "download/KevoreeRuntime.jar"

  //   def getPlatformStableJNLP = "http://dist.kevoree.org/KevoreeRuntimeStable.php"

  private def getEditorLastRelease = "download/KevoreeEditorLastRelease.jar"

  private def getRuntimeLastRelease = "download/KevoreeRuntimeLastRelease.jar"

  /* Snapshot */
  private def getEditorSnapshotJNLP = "download/KevoreeEditorSnapshot.jnlp"

  private def getEditorSnapshotJAR = "download/KevoreeEditorSnapshot.jar"

  //  def getEditorSnapshotJNLP = "http://dist.kevoree.org/KevoreeEditorSnapshot.php"

  private def getPlatformSnapshotJNLP = "download/KevoreeRuntimeSnapshot.jnlp"

  private def getPlatformSnapshotJAR = "download/KevoreeRuntimeSnapshot.jar"

  //  def getPlatformSnapshotJNLP = "http://dist.kevoree.org/KevoreeRuntimeSnapshot.php"

  private def getEditorLastSnapshot = "download/KevoreeEditorLastSnapshot.jar"

  private def getRuntimeLastSnapshot = "download/KevoreeRuntimeLastSnapshot.jar"

  private def getAndroidStableAPK = "download/KevoreeRuntimeAndroidRelease.apk"

  private def getAndroidSnapshotAPK = "download/KevoreeRuntimeAndroidSnapshot.jar"


  var variables = HashMap(
                           "editorStableJNLP" -> getEditorStableJNLP,
                           "platformStableJNLP" -> getPlatformStableJNLP,
                           "editorSnapshotJNLP" -> getEditorSnapshotJNLP,
                           "platformSnapshotJNLP" -> getPlatformSnapshotJNLP,
                           "editorStableJAR" -> getEditorStableJAR,
                           "platformStableJAR" -> getPlatformStableJAR,
                           "editorSnapshotJAR" -> getEditorSnapshotJAR,
                           "platformSnapshotJAR" -> getPlatformSnapshotJAR,
                           "androidStableAPK" -> getAndroidStableAPK,
                           "androidSnapshotAPK" -> getAndroidSnapshotAPK,
                           "kevoree.version.release" -> "toto",
                           "kevoree.version.snapshot" -> "titi"
                         )

  def getVariables = variables

  var editorLastRelease = ""
  var editorLastSnapshot = ""
  var runtimeLastRelease = ""
  var runtimeLastSnapshot = ""
  var androidRuntimeLastRelease = ""
  var androidRuntimeLastSnapshot = ""

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

          file = bootService.resolveArtifact("org.kevoree.platform.android.apk", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/release/"))
          updateAndroidRuntimeLastRelease(file.getAbsolutePath)
          file = bootService.resolveArtifact("org.kevoree.platform.android.apk", "org.kevoree.platform", "LATEST", List[String]("http://maven.kevoree.org/snapshots/"))
          updateAndroidRuntimeLastSnapshot(file.getAbsolutePath)

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
          case _@e => logger.debug("Unable to update maven artifact", e)
        }
      }
    }, 1, /*21*/ 300000)
    this
  }

  case class DOWNLOAD (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse)

  case class UPDATE_EDITOR_LAST_RELEASE (filePath: String)

  case class UPDATE_RUNTIME_LAST_RELEASE (filePath: String)

  case class UPDATE_EDITOR_LAST_SNAPSHOT (filePath: String)

  case class UPDATE_RUNTIME_LAST_SNAPSHOT (filePath: String)

  case class UPDATE_ANDROID_RUNTIME_RELEASE_VERSION (filePath: String)

  case class UPDATE_ANDROID_RUNTIME_SNAPSHOT_VERSION (filePath: String)

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

  def updateAndroidRuntimeLastRelease (filePath: String) {
    this ! UPDATE_ANDROID_RUNTIME_RELEASE_VERSION(filePath)
  }

  def updateAndroidRuntimeLastSnapshot (filePath: String) {
    this ! UPDATE_ANDROID_RUNTIME_SNAPSHOT_VERSION(filePath)
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
        case UPDATE_ANDROID_RUNTIME_RELEASE_VERSION(filePath) => androidRuntimeLastRelease = filePath
        case UPDATE_ANDROID_RUNTIME_SNAPSHOT_VERSION(filePath) => androidRuntimeLastSnapshot = filePath
        case UPDATE_RELEASE_VERSION(version) => {
          variables = variables.filterNot(v => v._1 == "kevoree.version.release") + ("kevoree.version.release" -> version)
          var pattern: String = mainSite.getDictionary.get("urlpattern").toString
          if (pattern.endsWith("**")) {
            pattern = pattern.replace("**", "")
          }
          if (!pattern.endsWith("/")) {
            pattern = pattern + "/"
          }
          logger.debug("invalidate download page: {}", pattern + "download")
          mainSite.invalidateCacheResponse(pattern + "download")
        }
        case UPDATE_SNAPSHOT_VERSION(version) => {
          variables = variables.filterNot(v => v._1 == "kevoree.version.snapshot") + ("kevoree.version.snapshot" -> version)
          var pattern: String = mainSite.getDictionary.get("urlpattern").toString
          if (pattern.endsWith("**")) {
            pattern = pattern.replace("**", "")
          }
          if (!pattern.endsWith("/")) {
            pattern = pattern + "/"
          }
          logger.debug("invalidate download page: {}", pattern + "download")
          mainSite.invalidateCacheResponse(pattern + "download")
        }
        case STOP() => timer.cancel(); timer.purge(); this.exit()
      }
    }
  }

  private def checkForDownloadInternals (index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    val handler = new URLHandlerScala()
    val urlPattern = origin.getDictionary.get("urlpattern").toString
    handler.getLastParam(request.getUrl, urlPattern) match {
      case Some(requestDownload) if (requestDownload == getEditorLastSnapshot || requestDownload == "/" + getEditorLastSnapshot) => {
        val bytes = getBytesForEditorLastSnapshot
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(editorLastSnapshot).lastModified()))

          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for snapshot editor")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getRuntimeLastSnapshot || requestDownload == "/" + getRuntimeLastSnapshot) => {
        val bytes = getBytesForRuntimeLastSnapshot
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(runtimeLastSnapshot).lastModified()))

          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for snapshot runtime")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getEditorLastRelease || requestDownload == "/" + getEditorLastRelease) => {
        val bytes = getBytesForEditorLastRelease
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(editorLastRelease).lastModified()))

          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Last-Modified", lastUpdated)
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for release editor")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getRuntimeLastRelease || requestDownload == "/" + getRuntimeLastRelease) => {
        val bytes = getBytesForRuntimeLastRelease
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(runtimeLastRelease).lastModified()))

          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Last-Modified", lastUpdated)
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for release runtime")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getEditorSnapshotJAR || requestDownload == "/" + getEditorSnapshotJAR) => {
        // TODO fix filename with Content-Disposition
        val bytes = getBytesForEditorLastSnapshot
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(editorLastSnapshot).lastModified()))

          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeEditor-" + getVariables("kevoree.version.snapshot") + ".jar; filename*=utf-8''KevoreeEditor-" +
            getVariables("kevoree.version.snapshot") + ".jar")
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for snapshot editor")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getPlatformSnapshotJAR || requestDownload == "/" + getPlatformSnapshotJAR) => {
        val bytes = getBytesForRuntimeLastSnapshot
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(runtimeLastSnapshot).lastModified()))

          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.snapshot") + ".jar; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.snapshot") + ".jar")
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for snapshot runtime")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getEditorStableJAR || requestDownload == "/" + getEditorStableJAR) => {
        val bytes = getBytesForEditorLastRelease
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(editorLastRelease).lastModified()))

          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Disposition",
                                   "attachment; filename=KevoreeEditor-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeEditor-" + getVariables("kevoree.version.release") +
                                     ".jar")
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for release editor")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getPlatformStableJAR || requestDownload == "/" + getPlatformStableJAR) => {
        val bytes = getBytesForRuntimeLastRelease
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(runtimeLastRelease).lastModified()))

          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.release") + ".jar")
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for release runtime")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getAndroidStableAPK || requestDownload == "/" + getAndroidStableAPK) => {
        val bytes = getBytesForAndroidRuntimeLastRelease
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(runtimeLastRelease).lastModified()))

          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.release") + ".jar")
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for release runtime")
          false
        }
      }
      case Some(requestDownload) if (requestDownload == getAndroidSnapshotAPK || requestDownload == "/" + getAndroidSnapshotAPK) => {
        val bytes = getBytesForAndroidRuntimeLastSnapshot
        if (bytes.length > 0) {
          val format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
          val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
          format.setCalendar(cal)
          val lastUpdated = format.format(new Date(new File(runtimeLastRelease).lastModified()))

          response.getHeaders.put("Content-Length", "" + bytes.length)
          response.getHeaders.put("Last-Modified", lastUpdated)
          response.getHeaders.put("Content-Disposition", "attachment; filename=KevoreeRuntime-" + getVariables("kevoree.version.release") + ".jar; filename*=utf-8''KevoreeRuntime-" +
            getVariables("kevoree.version.release") + ".jar")
          response.setRawContent(bytes)
          true
        } else {
          logger.warn("Unable to get the last jar for release runtime")
          false
        }
      }
      case _ => false
    }
  }

  private def getBytesForEditorLastRelease: Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(editorLastRelease))
  }

  private def getBytesForRuntimeLastRelease: Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(runtimeLastRelease))
  }

  private def getBytesForEditorLastSnapshot: Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(editorLastSnapshot))
  }

  private def getBytesForRuntimeLastSnapshot: Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(runtimeLastSnapshot))
  }

  private def getBytesForAndroidRuntimeLastRelease: Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(androidRuntimeLastSnapshot))
  }

  private def getBytesForAndroidRuntimeLastSnapshot: Array[Byte] = {
    FileNIOHelper.getBytesFromFile(new File(androidRuntimeLastSnapshot))
  }

  private def convertStreamToString (inputStream: InputStream): String = {
    val rand: Random = new Random
    val temp: File = File.createTempFile("kevoreeloaderLib" + rand.nextInt, ".xmi")
    temp.deleteOnExit()
    val out = new ByteArrayOutputStream()
    var read: Int = 0
    val bytes: Array[Byte] = new Array[Byte](1024)
    while ((({
      read = inputStream.read(bytes)
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
