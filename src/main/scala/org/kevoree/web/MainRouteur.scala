package org.kevoree.web

import ru.circumflex._, core._, web._
import io.Source

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 16:05
 */

class MainRouteur extends Router with PageRenderer {
  any("/") = krender("overview.html","/")
  any("/index") = krender("overview.html","/")
  any("/index.htm") = krender("overview.html","/")
  any("/index.html") = krender("overview.html","/")

  any("/core") = krender("core_features.html","/core")
  //any("/core") = krender("core_features.html")


}





