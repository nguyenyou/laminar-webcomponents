//> using scala 3.8.0
//> using platform js
//> using jsModuleKind esmodule
//> using dep com.raquo::laminar::17.2.1
//> using dep org.scala-js::scalajs-dom::2.8.1

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import com.raquo.laminar.nodes.DetachedRoot
import com.raquo.laminar.tags.CustomHtmlTag
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

// =============================================================================
// HelloWorld Web Component
// =============================================================================

class HelloWorld extends dom.HTMLElement {
  private var detachedRoot: Option[DetachedRoot[HtmlElement]] = None
  private val nameVar = Var("World")

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

  def render = {
    div(
      cls := "container",
      child.text <-- nameVar.signal.map(n => s"Hello, $n!")
    )
  }

  def connectedCallback(): Unit = {
    val shadow = this.attachShadow(new dom.ShadowRootInit {
      var mode = dom.ShadowRootMode.open
    })

    // Inject styles into shadow DOM
    val styleElement = dom.document.createElement("style")
    styleElement.textContent = styles
    shadow.appendChild(styleElement)

    // Render Laminar content
    val root = renderDetached(
      render,
      activateNow = true
    )
    detachedRoot = Some(root)
    shadow.appendChild(root.ref)
  }

  def disconnectedCallback(): Unit = {
    detachedRoot.foreach(_.deactivate())
    detachedRoot = None
  }

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
  @JSExportStatic
  val observedAttributes: js.Array[String] = js.Array("name")

  // Register this component with the browser
  def register(): Unit = {
    dom.window.customElements
      .define("hello-world", js.constructorOf[HelloWorld])
  }

  // Typed Laminar tag for using this component
  val tag: CustomHtmlTag[dom.HTMLElement] = CustomHtmlTag("hello-world")

  // Type-safe attribute
  val name: HtmlAttr[String] = htmlAttr("name", StringAsIsCodec)

  // Convenience constructor
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = tag(mods*)
}

// =============================================================================
// Main Application
// =============================================================================

@main
def main(): Unit = {
  // Register the custom element
  HelloWorld.register()

  // Use the component with typed API
  val app = div(
    input(
      typ := "text",
      placeholder := "Enter your name",
      padding := "10px",
      fontSize := "16px",
      marginBottom := "20px",
      inContext { thisNode =>
        onInput.mapToValue --> { value =>
          // Find the hello-world element and update its attribute
          thisNode.ref.parentElement
            .querySelector("hello-world")
            .setAttribute("name", value)
        }
      }
    ),
    HelloWorld(
      HelloWorld.name := "World"
    )
  )

  render(dom.document.getElementById("app"), app)
}
