import com.raquo.laminar.api.L.*
import org.scalajs.dom

// =============================================================================
// Main Application
// =============================================================================

@main
def main(): Unit = {
  // Register the custom element
  HelloWorld.register()

  // Use the component with typed API
  val app = div(
    input(
      typ := "text",
      placeholder := "Enter your name",
      padding := "10px",
      fontSize := "16px",
      marginBottom := "20px",
      inContext { thisNode =>
        onInput.mapToValue --> { value =>
          // Find the hello-world element and update its attribute
          thisNode.ref.parentElement
            .querySelector("hello-world")
            .setAttribute("name", value)
        }
      }
    ),
    HelloWorld(
      HelloWorld.name := "World"
    )
  )

  render(dom.document.getElementById("app"), app)
}
