import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import org.scalajs.dom

// =============================================================================
// Base Web Component - Handles lifecycle and Laminar integration
// =============================================================================

abstract class WebComponent extends dom.HTMLElement {
  private var _detachedRoot: Option[DetachedRoot[HtmlElement]] = None
  private var _props: List[(ReactiveAttr[?], ReactiveProp[?])] = Nil

  // Override to define component's styles
  def styles: String = ""

  // Override to define component's render tree
  def render: HtmlElement

  // Helper to create and register a reactive prop
  protected def prop[T](attr: ReactiveAttr[T]): ReactiveProp[T] = {
    val p = new ReactiveProp(attr)
    _props = _props :+ (attr, p)
    p
  }

  def connectedCallback(): Unit = {
    val shadow = this.attachShadow(new dom.ShadowRootInit {
      var mode = dom.ShadowRootMode.open
    })

    if (styles.nonEmpty) {
      val styleElement = dom.document.createElement("style")
      styleElement.textContent = styles
      shadow.appendChild(styleElement)
    }

    val root = renderDetached(render, activateNow = true)
    _detachedRoot = Some(root)
    shadow.appendChild(root.ref)
  }

  def disconnectedCallback(): Unit = {
    _detachedRoot.foreach(_.deactivate())
    _detachedRoot = None
  }

  def attributeChangedCallback(
      attrName: String,
      oldValue: String | Null,
      newValue: String | Null
  ): Unit = {
    _props.foreach { case (attr, prop) =>
      if (attr.attrName == attrName) {
        prop.asInstanceOf[ReactiveProp[Any]].handleChange(newValue)
      }
    }
  }
}
