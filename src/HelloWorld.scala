import com.raquo.laminar.api.L.*
import CssMacro.css

object HelloWorld extends LaminarWebComponent("hello-world") {
  val name = attr.string("name", "World")

  override def render: View = div(
    child.text <-- name.signal.map(n => s"Hello, $n!")
  )
}
