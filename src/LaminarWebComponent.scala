import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import com.raquo.laminar.tags.CustomHtmlTag
import org.scalajs.dom
import scala.scalajs.js

// =============================================================================
// LaminarWebComponent - Unified base class for defining web components
// =============================================================================

abstract class LaminarWebComponent(val tagName: String) { self =>

  // -------------------------------------------------------------------------
  // Attribute definitions (override in subclass)
  // -------------------------------------------------------------------------

  /** Override to define component attributes. Return a tuple or single
    * ReactiveAttr.
    */
  def attributes: Tuple | ReactiveAttr[?] | EmptyTuple = EmptyTuple

  // -------------------------------------------------------------------------
  // Attribute factory methods
  // -------------------------------------------------------------------------

  protected object attr {
    def string(name: String, default: String = ""): ReactiveAttr[String] =
      ReactiveAttr.string(name, default)
    def int(name: String, default: Int = 0): ReactiveAttr[Int] =
      ReactiveAttr.int(name, default)
    def boolean(name: String, default: Boolean = false): ReactiveAttr[Boolean] =
      ReactiveAttr.boolean(name, default)
    def double(name: String, default: Double = 0.0): ReactiveAttr[Double] =
      ReactiveAttr.double(name, default)
  }

  // -------------------------------------------------------------------------
  // Override to define styles and render logic
  // -------------------------------------------------------------------------

  /** Override to define component's CSS styles */
  def styles: String = ""

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

      if (self.styles.nonEmpty) {
        val styleElement = dom.document.createElement("style")
        styleElement.textContent = self.styles
        shadow.appendChild(styleElement)
      }

      given props: Props = new Props(this)
      _props = Some(props)

      val root = renderDetached(self.render, activateNow = true)
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

  /** Extracts attribute names from the attributes definition */
  private def observedAttributeNames: js.Array[String] = {
    val attrs = attributes
    val names = js.Array[String]()

    def collectNames(t: Any): Unit = t match {
      case EmptyTuple          => ()
      case ra: ReactiveAttr[?] => names.push(ra.attrName)
      case tuple: Tuple =>
        tuple.productIterator.foreach(collectNames)
      case _ => ()
    }

    collectNames(attrs)
    names
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
