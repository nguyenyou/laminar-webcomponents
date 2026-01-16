import com.raquo.laminar.api.L.*
import CssMacro.css

// =============================================================================
// HelloWorld Web Component
// =============================================================================

object HelloWorld extends LaminarWebComponent("hello-world") {
  val name = attr.string("name", "World")

  override def render(using Props): HtmlElement = {
    div(
      child.text <-- name.signal.map(n => s"Hello, $n!")
    )
  }
}
