package org.kevoree.web

import ru.circumflex.web.StandaloneServer
import ru.circumflex.core._

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 16:12
 */


object ServerApp extends App {
  cx("cx.router") = classOf[MainRouteur]
  val server = new StandaloneServer
  server.server.start()

}
