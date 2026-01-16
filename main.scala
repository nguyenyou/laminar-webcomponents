//> using scala 3.8.0
//> using platform js
//> using jsModuleKind esmodule
//> using dep com.raquo::laminar::17.2.1
//> using dep org.scala-js::scalajs-dom::2.8.1

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

// Define a custom web component
@js.native
@JSGlobal
class HTMLElement extends dom.HTMLElement

class HelloWorld extends HTMLElement {
  // Called when the element is added to the DOM
  def connectedCallback(): Unit = {
    val shadowRoot = this.attachShadow(new dom.ShadowRootInit {
      var mode = dom.ShadowRootMode.open
    })
    val container = dom.document.createElement("div")
    shadowRoot.appendChild(container)
    render(container, div("Hello, World!"))
  }
}

@main
def main(): Unit = {
  // Register the custom element
  dom.window.customElements.define("hello-world", js.constructorOf[HelloWorld])

  // Add the custom element to the page
  val app = dom.document.getElementById("app")
  app.innerHTML = "<hello-world></hello-world>"
}
