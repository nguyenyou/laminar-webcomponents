import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.{DetachedRoot, Slot}
import com.raquo.laminar.tags.CustomHtmlTag
import org.scalajs.dom
import scala.scalajs.js

export CssMacro.css

abstract class LaminarWebComponent(val tagName: String) {

  type ModFunction = this.type => Modifier[HtmlElement]

  type ComponentMod = Modifier[HtmlElement] | ModFunction

  private val _registeredAttrs =
    scala.collection.mutable.ListBuffer[ReactiveAttr[?]]()

  final def registeredAttributes: Seq[ReactiveAttr[?]] = _registeredAttrs.toSeq

  def styles: String = ""

  // Shared stylesheet - created once per component type, shared by all instances
  private lazy val sharedStylesheet: Option[dom.CSSStyleSheet] = {
    val css = styles
    if (css.nonEmpty) {
      val sheet = new dom.CSSStyleSheet()
      sheet.asInstanceOf[js.Dynamic].replaceSync(css)
      Some(sheet)
    } else None
  }

  protected object attr {
    private def register[A <: ReactiveAttr[?]](ra: A): ra.type = {
      _registeredAttrs += ra
      ra
    }

    def string(
        name: String,
        default: String = "",
        reflect: Boolean = false
    ): ReactiveAttr[String] =
      register(ReactiveAttr.string(name, default, reflect))

    transparent inline def stringUnion[T <: String](
        name: String,
        default: T,
        reflect: Boolean = false
    )(using uv: UnionValues[T]): StringUnionAttr[T] =
      register(StringUnionAttr[T](name, default, reflect))

    transparent inline def `enum`[E](
        name: String,
        default: E,
        reflect: Boolean = false
    )(using ev: EnumValues[E]): EnumAttr[E] =
      register(EnumAttr[E](name, default, reflect))

    def int(
        name: String,
        default: Int = 0,
        reflect: Boolean = false
    ): ReactiveAttr[Int] =
      register(ReactiveAttr.int(name, default, reflect))
    def boolean(
        name: String,
        default: Boolean = false,
        reflect: Boolean = false
    ): ReactiveAttr[Boolean] =
      register(ReactiveAttr.boolean(name, default, reflect))
    def double(
        name: String,
        default: Double = 0.0,
        reflect: Boolean = false
    ): ReactiveAttr[Double] =
      register(ReactiveAttr.double(name, default, reflect))
  }

  protected class Props(element: dom.HTMLElement) {
    private var _propMap: Map[String, ReactiveProp[?]] = Map.empty

    def apply[T](attr: ReactiveAttr[T]): ReactiveProp[T] = {
      _propMap.get(attr.attrName) match {
        case Some(p) => p.asInstanceOf[ReactiveProp[T]]
        case None =>
          val p = new ReactiveProp(element, attr)
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

  extension [T](dsl: ReactiveAttrDsl[T])(using props: Props) {
    def signal: Signal[T] = props(dsl.reactiveAttr).signal
    def get: T = props(dsl.reactiveAttr).get
    def set(value: T): Unit = props(dsl.reactiveAttr).set(value)
    def update(f: T => T): Unit = props(dsl.reactiveAttr).update(f)
  }

  private class ComponentInstance extends dom.HTMLElement {
    private var _detachedRoot: Option[DetachedRoot[HtmlElement]] = None
    private var _instanceProps: Option[Props] = None

    def connectedCallback(): Unit = {
      val shadow = this.attachShadow(new dom.ShadowRootInit {
        var mode = dom.ShadowRootMode.open
      })

      // Use adoptedStyleSheets to share a single CSSStyleSheet across all instances
      sharedStylesheet.foreach { sheet =>
        shadow.asInstanceOf[js.Dynamic].adoptedStyleSheets = js.Array(sheet)
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

  def apply(modFns: ModFunction*)(mods: Modifier[HtmlElement]*): HtmlElement = {
    register
    val el = CustomHtmlTag[dom.HTMLElement](tagName)()
    modFns.foreach { modFn =>
      modFn(this)(el)
    }
    mods.foreach { mod =>
      mod(el)
    }
    el
  }
}
