import com.raquo.laminar.api.L.*
import CssMacro.css

// =============================================================================
// HelloWorld Web Component
// =============================================================================

object HelloWorld extends LaminarWebComponent("hello-world") {

  private val (_styles, classNames) = css"""
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

  val name = attr.string("name", "World")

  override def attributes = name

  override def styles = _styles

  override def render(using Props): HtmlElement = {
    div(
      cls := classNames.container,
      child.text <-- name.signal.map(n => s"Hello, $n!")
    )
  }
}
