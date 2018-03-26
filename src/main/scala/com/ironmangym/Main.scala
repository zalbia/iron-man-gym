package com.ironmangym

import com.ironmangym.CssSettings._
import com.ironmangym.domain.SPACircuit
import com.ironmangym.logout._
import com.ironmangym.about.About
import com.pangwarta.sjrmui._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object Main {

  sealed trait Page
  object Page {
    case object Landing extends Page
    case object Profile extends Page
    case object About extends Page
  }

  val routerConfig: RouterConfig[Page] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    (
      staticRoute(root, Page.Landing) ~> renderR(ctl => SPACircuit.wrap(_.users)(proxy => Registration(ctl, proxy)))
      | staticRoute("#about", Page.About) ~> render(About())
    ).notFound(redirectToPage(Page.Landing)(Redirect.Replace))
  }.renderWith(layout)

  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    CssBaseline(
      <.div(
        AppBar(position = AppBar.static, color = AppBar.default)()(
          Toolbar()()(
            <.img(
              ^.src := "./target/scala-2.12/classes/ironmangym_logo.png",
              ^.height := 40.px, ^.width := 40.px, ^.marginRight := 12.px
            ),
            Typography(variant = Typography.Variant.headline, color = Typography.Color.inherit)()(
              "Iron Man Gym"
            )
          )
        ),
        <.div(
          ^.id := "container",
          r.render()
        )
      ).rawElement
    )

  def main(args: Array[String]): Unit = {
    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(dom.document.getElementById("root"))
    Styles.addToDocument()
  }
}
