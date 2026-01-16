//> using scala 3.8.0
//> using platform js
//> using jsModuleKind esmodule
//> using dep com.raquo::laminar::17.2.1
//> using dep org.scala-js::scalajs-dom::2.8.1

import com.raquo.laminar.api.L.*
import org.scalajs.dom

@main
def main(): Unit = {
  val app = div(
    h1("Welcome to Laminar"),
    p("This is a Scala.js web component built with Laminar")
  )

  val container = dom.document.getElementById("app")
  render(container, app)
}
