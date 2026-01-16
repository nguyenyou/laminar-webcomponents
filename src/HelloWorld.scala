import com.raquo.laminar.api.L.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*

// =============================================================================
// HelloWorld Web Component
// =============================================================================

class HelloWorld extends WebComponent {
  import HelloWorld.attrs

  private val nameProp = prop(attrs.name)

  override def styles: String = """
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

  override def render: HtmlElement = {
    div(
      cls := "container",
      child.text <-- nameProp.signal.map(n => s"Hello, $n!")
    )
  }
}

object HelloWorld
    extends WebComponentCompanion[HelloWorld]("hello-world")(
      using () => js.constructorOf[HelloWorld]
    ) {
  object attrs {
    val name = ReactiveAttr.string("name", "World")
  }

  @JSExportStatic
  val observedAttributes: js.Array[String] =
    extractObservedAttributes[attrs.type]

  val name: HtmlAttr[String] = stringAttr("name")
}
