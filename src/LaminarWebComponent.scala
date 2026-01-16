import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import com.raquo.laminar.tags.CustomHtmlTag
import org.scalajs.dom
import scala.scalajs.js

// =============================================================================
// LaminarWebComponent - Unified base class for defining web components
// =============================================================================

abstract class LaminarWebComponent(val tagName: String) {

  // -------------------------------------------------------------------------
  // Attribute definitions (auto-registered via attr.* methods)
  // -------------------------------------------------------------------------

  private val _registeredAttrs =
    scala.collection.mutable.ListBuffer[ReactiveAttr[?]]()

  /** All registered attributes for this component (auto-collected) */
  final def registeredAttributes: Seq[ReactiveAttr[?]] = _registeredAttrs.toSeq

  // -------------------------------------------------------------------------
  // Style definitions (override in subclass)
  // -------------------------------------------------------------------------

  /** Override to define component styles. Can return a String or css macro
    * result.
    */
  def styles: String = ""

  // -------------------------------------------------------------------------
  // Attribute factory methods (auto-registering)
  // -------------------------------------------------------------------------

  protected object attr {
    private def register[T](ra: ReactiveAttr[T]): ReactiveAttr[T] = {
      _registeredAttrs += ra
      ra
    }

    def string(name: String, default: String = ""): ReactiveAttr[String] =
      register(ReactiveAttr.string(name, default))
    def int(name: String, default: Int = 0): ReactiveAttr[Int] =
      register(ReactiveAttr.int(name, default))
    def boolean(name: String, default: Boolean = false): ReactiveAttr[Boolean] =
      register(ReactiveAttr.boolean(name, default))
    def double(name: String, default: Double = 0.0): ReactiveAttr[Double] =
      register(ReactiveAttr.double(name, default))
  }

  // -------------------------------------------------------------------------
  // Style and render logic
  // -------------------------------------------------------------------------

  /** Override to define component's render tree. Use attr.signal, attr.get,
    * attr.set directly - Props is implicitly available.
    */
  def render(using Props): HtmlElement

  // Extension methods to access reactive props directly from attributes
  extension [T](attr: ReactiveAttr[T])(using props: Props) {
    def signal: Signal[T] = props(attr).signal
    def get: T = props(attr).get
    def set(value: T): Unit = props(attr).set(value)
    def update(f: T => T): Unit = props(attr).set(f(props(attr).get))
  }

  // -------------------------------------------------------------------------
  // Props - runtime reactive property access
  // -------------------------------------------------------------------------

  class Props(element: dom.HTMLElement) {
    private var _props: Map[String, ReactiveProp[?]] = Map.empty

    def apply[T](attr: ReactiveAttr[T]): ReactiveProp[T] = {
      _props.get(attr.attrName) match {
        case Some(p) => p.asInstanceOf[ReactiveProp[T]]
        case None =>
          val p = new ReactiveProp(attr)
          // Initialize from current attribute value
          val currentValue = element.getAttribute(attr.attrName)
          if (currentValue != null) {
            p.handleChange(currentValue)
          }
          _props = _props + (attr.attrName -> p)
          p
      }
    }

    def handleAttributeChange(
        attrName: String,
        newValue: String | Null
    ): Unit = {
      _props.get(attrName).foreach { prop =>
        prop.asInstanceOf[ReactiveProp[Any]].handleChange(newValue)
      }
    }
  }

  // -------------------------------------------------------------------------
  // Internal: Web Component class generation
  // -------------------------------------------------------------------------

  private class ComponentInstance extends dom.HTMLElement {
    private var _detachedRoot: Option[DetachedRoot[HtmlElement]] = None
    private var _props: Option[Props] = None

    def connectedCallback(): Unit = {
      val shadow = this.attachShadow(new dom.ShadowRootInit {
        var mode = dom.ShadowRootMode.open
      })

      if (styles.nonEmpty) {
        val styleElement = dom.document.createElement("style")
        styleElement.textContent = styles
        shadow.appendChild(styleElement)
      }

      given props: Props = new Props(this)
      _props = Some(props)

      val root = renderDetached(render, activateNow = true)
      _detachedRoot = Some(root)
      shadow.appendChild(root.ref)
    }

    def disconnectedCallback(): Unit = {
      _detachedRoot.foreach(_.deactivate())
      _detachedRoot = None
      _props = None
    }

    def attributeChangedCallback(
        attrName: String,
        oldValue: String | Null,
        newValue: String | Null
    ): Unit = {
      _props.foreach(_.handleAttributeChange(attrName, newValue))
    }
  }

  // -------------------------------------------------------------------------
  // Registration & Laminar integration
  // -------------------------------------------------------------------------

  /** Extracts attribute names from registered attributes */
  private def observedAttributeNames: js.Array[String] = {
    js.Array(_registeredAttrs.map(_.attrName).toSeq*)
  }

  /** Register this web component with the browser */
  lazy val register: Unit = {
    val ctor = js.constructorOf[ComponentInstance]
    ctor.observedAttributes = observedAttributeNames
    dom.window.customElements.define(tagName, ctor)
  }

  /** Create an instance of this component in Laminar */
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = {
    register // Ensure registered
    CustomHtmlTag[dom.HTMLElement](tagName)(mods*)
  }
}
