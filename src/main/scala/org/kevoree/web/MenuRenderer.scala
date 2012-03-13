package org.kevoree.web

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/03/12
 * Time: 16:59
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
              {
              getItems.map(it =>{
                <li class={if(currentURL == it._2){"active"}else{"noactive"}}><a href={it._2}>{it._1}</a></li>
              })
              }
            </ul>
            <ul class="nav pull-right">
              <li><a href="http://github.com/dukeboard/kevoree">Get it on Github</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>.toString()
  }


  def getItems : List[Tuple3[String,String,String]] = {
    List(
      ("Home","/","overview.html"),
      ("Core Features","/core","core_features.html"),
      ("Tools","/tools",""),
      ("Platforms","/platform",""),
      ("Download","/download","download.html"),
      ("The KTeam","/kteam","")
    )
  }

}
