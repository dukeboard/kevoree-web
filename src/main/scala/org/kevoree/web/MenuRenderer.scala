package org.kevoree.web

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */

object MenuRenderer {

  def getMenuHtml(currentURL : String) : String = {
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="i-bar"><i class="icon-chevron-down icon-white"></i></span>
          </a>
          <a class="brand" href="index.html">Kevoree</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li class={if(currentURL == "/"){"active"}else{"noactive"}}><a href="/">Home</a></li>
              <li class={if(currentURL == "/core"){"active"}else{"noactive"}}><a href="/core">Core Features</a></li>
            </ul>
            <ul class="nav pull-right">
              <li><a href="http://github.com/dukeboard/kevoree">Get it on Github</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>.toString()
  }


  def getItems : List[Tuple2[String,String]] = {
    List("core")
  }

}
