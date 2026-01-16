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
  import HelloWorld.attrs

  private var detachedRoot: Option[DetachedRoot[HtmlElement]] = None

  // Create reactive props from attribute definitions
  private val nameProp = ReactiveProp(attrs.name)
  private val countProp = ReactiveProp(attrs.count)

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

  def connectedCallback(): Unit = {
    val shadow = this.attachShadow(new dom.ShadowRootInit {
      var mode = dom.ShadowRootMode.open
    })

    val styleElement = dom.document.createElement("style")
    styleElement.textContent = styles
    shadow.appendChild(styleElement)

    val root = renderDetached(
      div(
        cls := "container",
        child.text <-- nameProp.signal.combineWith(countProp.signal).map {
          case (name, count) => s"Hello, $name! (count: $count)"
        }
      ),
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
      attrName: String,
      oldValue: String | Null,
      newValue: String | Null
  ): Unit = {
    attrs.handleChange(attrName, newValue)(
      attrs.name -> nameProp,
      attrs.count -> countProp
    )
  }
}

object HelloWorld {
  // Define attributes - macro will extract names for observedAttributes
  object attrs {
    val name = ReactiveAttr.string("name", "World")
    val count = ReactiveAttr.int("count", 0)

    // Helper to dispatch attribute changes
    def handleChange(attrName: String, value: String | Null)(
        handlers: (ReactiveAttr[?], ReactiveProp[?])*
    ): Unit = {
      handlers.foreach { case (attr, prop) =>
        if (attr.attrName == attrName) {
          prop.asInstanceOf[ReactiveProp[Any]].handleChange(value)
        }
      }
    }
  }

  // Macro-generated observedAttributes from attrs object
  @JSExportStatic
  val observedAttributes: js.Array[String] =
    extractObservedAttributes[attrs.type]

  def register(): Unit = {
    dom.window.customElements
      .define("hello-world", js.constructorOf[HelloWorld])
  }

  val tag: CustomHtmlTag[dom.HTMLElement] = CustomHtmlTag("hello-world")
  val name: HtmlAttr[String] = htmlAttr("name", StringAsIsCodec)

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = tag(mods*)
}
