import com.raquo.laminar.api.L.*

// =============================================================================
// HelloCounter Web Component - A counter with increment/decrement buttons
// =============================================================================

object HelloCounter extends LaminarWebComponent("hello-counter") {

  val count = attr.int("count", 0)
  val step = attr.int("step", 1)
  val label = attr.string("label", "Counter")

  override def attributes = (count, step, label)

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

  override def render(using Props): HtmlElement = {
    div(
      cls := "counter",
      renderLabel,
      renderCount,
      renderButtons
    )
  }

  private def renderLabel(using Props): HtmlElement = {
    span(cls := "label", child.text <-- label.signal)
  }

  private def renderCount(using Props): HtmlElement = {
    span(cls := "count", child.text <-- count.signal.map(_.toString))
  }

  private def renderButtons(using Props): HtmlElement = {
    div(
      cls := "buttons",
      button(
        "−",
        onClick --> { _ => count.update(_ - step.get) }
      ),
      button(
        "+",
        onClick --> { _ => count.update(_ + step.get) }
      )
    )
  }
}
