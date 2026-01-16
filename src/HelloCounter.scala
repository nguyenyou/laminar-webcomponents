import com.raquo.laminar.api.L.*
import com.raquo.laminar.tags.CustomHtmlTag
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.*

// =============================================================================
// HelloCounter Web Component - A counter with increment/decrement buttons
// =============================================================================

class HelloCounter extends WebComponent {
  import HelloCounter.attrs

  // Reactive props from attributes
  private val countProp = prop(attrs.count)
  private val stepProp = prop(attrs.step)
  private val labelProp = prop(attrs.label)

  override def styles: String = """
    .counter {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 12px;
      font-family: system-ui, sans-serif;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
      gap: 16px;
    }
    .label {
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 2px;
      opacity: 0.9;
    }
    .count {
      font-size: 48px;
      font-weight: bold;
    }
    .buttons {
      display: flex;
      gap: 12px;
    }
    button {
      padding: 12px 24px;
      font-size: 18px;
      font-weight: bold;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      transition: transform 0.1s, box-shadow 0.1s;
      background: white;
      color: #667eea;
    }
    button:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }
    button:active {
      transform: translateY(0);
    }
  """

  override def render: HtmlElement = {
    div(
      cls := "counter",
      span(cls := "label", child.text <-- labelProp.signal),
      span(cls := "count", child.text <-- countProp.signal.map(_.toString)),
      div(
        cls := "buttons",
        button(
          "−",
          onClick --> { _ =>
            countProp.set(countProp.get - stepProp.get)
          }
        ),
        button(
          "+",
          onClick --> { _ =>
            countProp.set(countProp.get + stepProp.get)
          }
        )
      )
    )
  }
}

object HelloCounter {
  object attrs {
    val count = ReactiveAttr.int("count", 0)
    val step = ReactiveAttr.int("step", 1)
    val label = ReactiveAttr.string("label", "Counter")
  }

  @JSExportStatic
  val observedAttributes: js.Array[String] =
    extractObservedAttributes[attrs.type]

  def register(): Unit = {
    dom.window.customElements
      .define("hello-counter", js.constructorOf[HelloCounter])
  }

  val tag: CustomHtmlTag[dom.HTMLElement] = CustomHtmlTag("hello-counter")

  val count: HtmlAttr[Int] =
    htmlAttr("count", com.raquo.laminar.codecs.IntAsStringCodec)
  val step: HtmlAttr[Int] =
    htmlAttr("step", com.raquo.laminar.codecs.IntAsStringCodec)
  val label: HtmlAttr[String] =
    htmlAttr("label", com.raquo.laminar.codecs.StringAsIsCodec)

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = tag(mods*)
}
