import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.DetachedRoot
import com.raquo.laminar.tags.CustomHtmlTag
import org.scalajs.dom
import scala.scalajs.js

abstract class LaminarWebComponent(val tagName: String) {

  private val _registeredAttrs =
    scala.collection.mutable.ListBuffer[ReactiveAttr[?]]()

  final def registeredAttributes: Seq[ReactiveAttr[?]] = _registeredAttrs.toSeq

  def styles: String = ""

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

  protected class Props(element: dom.HTMLElement) {
    private var _propMap: Map[String, ReactiveProp[?]] = Map.empty

    def apply[T](attr: ReactiveAttr[T]): ReactiveProp[T] = {
      _propMap.get(attr.attrName) match {
        case Some(p) => p.asInstanceOf[ReactiveProp[T]]
        case None =>
          val p = new ReactiveProp(attr)
          val currentValue = element.getAttribute(attr.attrName)
          if (currentValue != null) {
            p.handleChange(currentValue)
          }
          _propMap = _propMap + (attr.attrName -> p)
          p
      }
    }

    def handleAttributeChange(
        attrName: String,
        newValue: String | Null
    ): Unit = {
      _propMap.get(attrName).foreach { prop =>
        prop.asInstanceOf[ReactiveProp[Any]].handleChange(newValue)
      }
    }
  }

  type View = Props ?=> HtmlElement

  def render: View

  protected def slotElement(name: String = ""): HtmlElement = {
    val el = dom.document.createElement("slot").asInstanceOf[dom.html.Element]
    if (name.nonEmpty) el.setAttribute("name", name)
    foreignHtmlElement(el)
  }

  extension [T](attr: ReactiveAttr[T])(using props: Props) {
    def signal: Signal[T] = props(attr).signal
    def get: T = props(attr).get
    def set(value: T): Unit = props(attr).set(value)
    def update(f: T => T): Unit = props(attr).update(f)
  }

  private class ComponentInstance extends dom.HTMLElement {
    private var _detachedRoot: Option[DetachedRoot[HtmlElement]] = None
    private var _instanceProps: Option[Props] = None

    def connectedCallback(): Unit = {
      val shadow = this.attachShadow(new dom.ShadowRootInit {
        var mode = dom.ShadowRootMode.open
      })

      if (styles.nonEmpty) {
        val styleElement = dom.document.createElement("style")
        styleElement.textContent = styles
        shadow.appendChild(styleElement)
      }

      given instanceProps: Props = new Props(this)
      _instanceProps = Some(instanceProps)

      val root = renderDetached(render(using instanceProps), activateNow = true)
      _detachedRoot = Some(root)
      shadow.appendChild(root.ref)
    }

    def disconnectedCallback(): Unit = {
      _detachedRoot.foreach(_.deactivate())
      _detachedRoot = None
      _instanceProps = None
    }

    def attributeChangedCallback(
        attrName: String,
        oldValue: String | Null,
        newValue: String | Null
    ): Unit = {
      _instanceProps.foreach(_.handleAttributeChange(attrName, newValue))
    }
  }

  private def observedAttributeNames: js.Array[String] = {
    js.Array(_registeredAttrs.map(_.attrName).toSeq*)
  }

  lazy val register: Unit = {
    val ctor = js.constructorOf[ComponentInstance]
    ctor.observedAttributes = observedAttributeNames
    dom.window.customElements.define(tagName, ctor)
  }

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = {
    register
    CustomHtmlTag[dom.HTMLElement](tagName)(mods*)
  }
}
