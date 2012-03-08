package org.kevoree.web

import io.Source

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 17:15
 */

trait PageRenderer {
  def krender(name : String,currentURL : String): String = {
    val sb = new StringBuffer()
    sb.append(MenuRenderer.getMenuHtml(currentURL))
    sb.append(kheader)
    sb.append("<div class=\"container\">")
    sb.append(renderHtml(name))
    sb.append("<footer>\n<p>&copy; Kevoree.org 2012</p>\n</footer>\n\n</div>")
    sb.append(footer)
    sb.append("</body></html>")
    sb.toString
  }

  def renderHtml(name: String): String = {
    val st = getClass.getClassLoader.getResourceAsStream("templates/html/" + name)
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
      "<link href=\"css/bootstrap.css\" rel=\"stylesheet\">" +
      "<link href=\"css/bootstrap-responsive.css\" rel=\"stylesheet\">" +
      "<link href=\"css/kevoree.css\" rel=\"stylesheet\">" +
      "<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>\n" +
      "<link href=\"js/google-code-prettify/prettify.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
      "<script type=\"text/javascript\" src=\"js/google-code-prettify/prettify.js\"></script>" +
      "</head>" +
      "<body onload=\"prettyPrint()\" style=\"background-image: url(http://subtlepatterns.com/patterns/whitey.png); background-attachment: initial; background-origin: initial; background-clip: initial; background-color: initial; background-position: initial initial; background-repeat: initial initial; \">\n"
  }

  def footer: String = {
    <script src="js/jquery.js"></script>
      <script src="js/bootstrap-transition.js"></script>
      <script src="js/bootstrap-alert.js"></script>
      <script src="js/bootstrap-modal.js"></script>
      <script src="js/bootstrap-dropdown.js"></script>
      <script src="js/bootstrap-scrollspy.js"></script>
      <script src="js/bootstrap-tab.js"></script>
      <script src="js/bootstrap-tooltip.js"></script>
      <script src="js/bootstrap-popover.js"></script>
      <script src="js/bootstrap-button.js"></script>
      <script src="js/bootstrap-collapse.js"></script>
      <script src="js/bootstrap-carousel.js"></script>
      <script src="js/bootstrap-typeahead.js"></script>.mkString
  }

}
