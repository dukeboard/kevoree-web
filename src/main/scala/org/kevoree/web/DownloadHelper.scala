package org.kevoree.web

import collection.immutable.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 25/03/12
 * Time: 23:04
 */

object DownloadHelper {

  def getLastVersion = "1.6.0-BETA7"

  /* Stable */
  def getEditorStableJNLP = "http://dist.kevoree.org/KevoreeEditorStable.php"

  def getPlatformStableJNLP = "http://dist.kevoree.org/KevoreeRuntimeStable.php"

  /* Snapshot */
  def getEditorSnapshotJNLP = "http://dist.kevoree.org/KevoreeEditorSnapshot.php"

  def getPlatformSnapshotJNLP = "http://dist.kevoree.org/KevoreeRuntimeSnapshot.php"


  def getVariables = HashMap(
    "editorStableJNLP"->getEditorStableJNLP,
    "platformStableJNLP"->getPlatformStableJNLP,
    "editorSnapshotJNLP"->getEditorSnapshotJNLP,
    "platformSnapshotJNLP"->getPlatformSnapshotJNLP
  )

}
