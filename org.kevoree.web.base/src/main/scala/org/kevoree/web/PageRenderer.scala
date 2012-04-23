package org.kevoree.web

import io.Source
import java.io._
import collection.immutable.HashMap
import org.kevoree.library.javase.webserver.{URLHandlerScala, AbstractPage, KevoreeHttpRequest, KevoreeHttpResponse}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 17:15
 */

class PageRenderer(devmod:Boolean,folder:java.io.File) {

  val handler = new URLHandlerScala()

  def checkForTemplateRequest(index: String, origin: AbstractPage, request: KevoreeHttpRequest, response: KevoreeHttpResponse): Boolean = {
    val urlPattern = origin.getDictionary.get("urlpattern").toString
    handler.getLastParam(request.getUrl, urlPattern) match {
      case Some(reqP) => {
        if (reqP == "" || reqP == null || reqP == "/") {
          response.setContent(krender(index, "/", HashMap[String, String](), urlPattern))
          true
        } else {
          MenuRenderer.getItems.find(it => {
            var patternCleaned = urlPattern
            if (patternCleaned.endsWith("**")) {
              patternCleaned = patternCleaned.replace("**", "");
            }
            if (!patternCleaned.endsWith("/")) {
              patternCleaned = patternCleaned + "/";
            }

            val cleanupRequest = it._2.replace("{urlpattern}", "/")
            val cleanupRep = if (!reqP.startsWith("/")) {
              "/" + reqP
            } else {
              reqP
            }
            cleanupRequest == (cleanupRep)
          }) match {
            case Some(it) => {
              response.setContent(krender(it._3, it._2, it._4, urlPattern))
              true
            }
            case None => false
          }
        }
      }
      case None => false
    }
  }

  def krender(name: String, currentURL: String, vars: HashMap[String, String], pattern: String): String = {
    val sb = new StringBuffer()
    sb.append(kheader)
    sb.append(MenuRenderer.getMenuHtml(currentURL))

    // sb.append("<div class=\"hero-unit\">\n    <div class=\"row\">\n        <div class=\"span5\">\n            <p><img src=\"img/kevoree-logo.png\"/></p>\n        </div>\n        <div class=\"span5\">\n            <p>Kevoree project aims at enabling distributed reconfigurable software development. Build around a component model, Kevoree leverage model@runtime approach to offer tools to build, adapt and synchronize distributed systems.\n                Extensible, this project already offer runtime for Standard Java Virtual Machine, Android, Arduino but also for virtualization management such as VirtualBox.\n                In short Kevoree helping you to develop your adaptable software from Cloud stack to embedded devices !\n            </p>\n        </div>\n    </div>\n</div>")
    sb.append("<div class=\"wrapper\">")
    sb.append("<div class=\"container\">")
    sb.append(replaceVariable(renderHtml(name), vars))
    sb.append("</div>")
    sb.append("<div class=\"push\"><!--//--></div>")
    sb.append("</div>")
    sb.append(footerScript)
    sb.append("<div class=\"footer\" /><footer>\n<p>&copy; Kevoree.org 2012</p>\n</footer>\n\n</div>")
    sb.append("</body></html>")

    var patternCleaned = pattern
    if (patternCleaned.endsWith("**")) {
      patternCleaned = patternCleaned.replace("**", "")
    }
    if (!patternCleaned.endsWith("/")) {
      patternCleaned = patternCleaned + "/"
    }
    sb.toString.replace("{urlpattern}", patternCleaned)
  }

  def renderHtml(name: String): String = {
    //Source.fromFile(new File(getClass.getClassLoader.getResource("templates/../").getPath+"../../src/main/resources/templates/html/" + name)).getLines().mkString("\n")

    var st :InputStream = null
    if (devmod)
      st = new FileInputStream(new File(folder.getAbsolutePath + java.io.File.separator+"templates"+ java.io.File.separator + "html"+ java.io.File.separator + name))
    else
      st = getClass.getClassLoader.getResourceAsStream("templates/html/" + name)
    if (st != null) {
      Source.fromInputStream(st).getLines().mkString("\n")
    } else {
      "not found"
    }


  }

  def kheader: String = {
    "<html lang=\"en\">" +
      "<head><meta charset=\"utf-8\">" +
      "<title>Kevoree Project</title>" +
      "<meta name=\"description\" content=\"Kevoree : Distributed Model@Runtime project\">" +
      "<meta name=\"author\" content=\"François Fouquet\">" +
      "<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->\n    <!--[if lt IE 9]>\n    <script src=\"//html5shim.googlecode.com/svn/trunk/html5.js\"></script>\n    <![endif]-->" +
    "<script type=\"text/javascript\" src=\"{urlpattern}js/bootstrap-carousel.js\"></script>" +
    "<link href=\"{urlpattern}css/bootstrap.min.css\" rel=\"stylesheet\">" +
    "<link href=\"{urlpattern}css/bootstrap-responsive.min.css\" rel=\"stylesheet\">" +
    "<link href=\"{urlpattern}css/kevoree.css\" rel=\"stylesheet\">" +
      "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>\n" +
      "<link href=\"{urlpattern}js/google-code-prettify/prettify.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
      "<script type=\"text/javascript\" src=\"{urlpattern}js/google-code-prettify/prettify.js\"></script>" +
      "</head>" +
      "<body onload=\"prettyPrint()\">\n"
  }


  def replaceVariable(html: String, vars: HashMap[String, String]): String = {
    var content = html
    vars.foreach(v => {
      content = content.replace("{" + v._1 + "}", v._2)
    })
    content
  }


  def footerScript: String = {
    <script src={"{urlpattern}js/jquery.js"}></script>
      <script src={"{urlpattern}js/bootstrap-transition.js"}></script>
      <script src={"{urlpattern}js/bootstrap-alert.js"}></script>
      <script src={"{urlpattern}js/bootstrap-modal.js"}></script>
      <script src={"{urlpattern}js/bootstrap-dropdown.js"}></script>
      <script src={"{urlpattern}js/bootstrap-scrollspy.js"}></script>
      <script src={"{urlpattern}js/bootstrap-tab.js"}></script>
      <script src={"{urlpattern}js/bootstrap-tooltip.js"}></script>
      <script src={"{urlpattern}js/bootstrap-popover.js"}></script>
      <script src={"{urlpattern}js/bootstrap-button.js"}></script>
      <script src={"{urlpattern}js/bootstrap-collapse.js"}></script>
      <script src={"{urlpattern}js/bootstrap-carousel.js"}></script>
      <script src={"{urlpattern}js/bootstrap-typeahead.js"}></script>.mkString

  }

}
