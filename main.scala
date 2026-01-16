//> using scala 3.8.0
//> using platform js
//> using jsModuleKind esmodule
//> using dep com.raquo::laminar::17.2.1
//> using dep org.scala-js::scalajs-dom::2.8.1

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

// Define a custom web component
@js.native
@JSGlobal
class HTMLElement extends dom.HTMLElement

class HelloWorld extends dom.HTMLElement {
  private var detachedRoot: Option[DetachedRoot[HtmlElement]] = None

  // Called when the element is added to the DOM
  def connectedCallback(): Unit = {
    println("connectedCallback")
    val shadowRoot = this.attachShadow(new dom.ShadowRootInit {
      var mode = dom.ShadowRootMode.open
    })
    val element = renderDetached(div("Hello, World!"), activateNow = true)
    detachedRoot = Some(element)
    shadowRoot.appendChild(element.ref)
  }

  // Called when the element is removed from the DOM
  def disconnectedCallback(): Unit = {
    println("disconnectedCallback")
    detachedRoot.foreach(_.deactivate())
    detachedRoot = None
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
