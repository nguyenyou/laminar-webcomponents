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
  private val nameVar = Var("World")

  // Define styles similar to Lit's css`` tagged template
  private val styles: String = """
    .container {
      padding: 20px;
      background-color: #4a90d9;
      color: white;
      border-radius: 8px;
      font-family: system-ui, sans-serif;
      font-size: 24px;
      text-align: center;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
  """

  // Called when the element is added to the DOM
  def connectedCallback(): Unit = {
    println("connectedCallback")
    val shadowRoot = this.attachShadow(new dom.ShadowRootInit {
      var mode = dom.ShadowRootMode.open
    })

    // Append styles to shadow root (like Lit does)
    val styleElement = dom.document.createElement("style")
    styleElement.textContent = styles
    shadowRoot.appendChild(styleElement)

    // Append content to shadow root
    val element = renderDetached(
      div(
        cls := "container",
        child.text <-- nameVar.signal.map(n => s"Hello, $n!")
      ),
      activateNow = true
    )
    detachedRoot = Some(element)
    shadowRoot.appendChild(element.ref)
  }

  // Called when the element is removed from the DOM
  def disconnectedCallback(): Unit = {
    println("disconnectedCallback")
    detachedRoot.foreach(_.deactivate())
    detachedRoot = None
  }

  // Called when an observed attribute changes
  def attributeChangedCallback(
      name: String,
      oldValue: String | Null,
      newValue: String | Null
  ): Unit = {
    println(
      s"attributeChangedCallback: $name changed from $oldValue to $newValue"
    )
    name match {
      case "name" => nameVar.set(Option(newValue).getOrElse("World"))
      case _      => ()
    }
  }
}

object HelloWorld {
  // Static getter for observed attributes
  val observedAttributes: js.Array[String] = js.Array("name")
}

@main
def main(): Unit = {
  // Register the custom element with observedAttributes
  val helloWorldClass = js.constructorOf[HelloWorld]
  // Set the static observedAttributes property on the class constructor
  helloWorldClass.asInstanceOf[js.Dynamic].observedAttributes =
    HelloWorld.observedAttributes
  dom.window.customElements.define("hello-world", helloWorldClass)

  // Add the custom element to the page
  val container = dom.document.getElementById("app")
  val helloWorldElement =
    dom.document.createElement("hello-world").asInstanceOf[dom.html.Element]

  val app = renderDetached(
    div(
      input(
        typ := "text",
        placeholder := "Enter your name",
        padding := "10px",
        fontSize := "16px",
        marginBottom := "20px",
        onInput.mapToValue --> { value =>
          println(s"input: $value")
          helloWorldElement.setAttribute("name", value)
        }
      ),
      foreignHtmlElement(helloWorldElement)
    ),
    activateNow = true
  )
  container.appendChild(app.ref)
}
