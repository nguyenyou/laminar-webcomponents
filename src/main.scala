import com.raquo.laminar.api.L.*
import org.scalajs.dom

// =============================================================================
// Main Application
// =============================================================================

@main
def main(): Unit = {
  // Force initialization (registration happens automatically via the macro)
  val _ = (HelloWorld.api, HelloCounter.api)

  val app = div(
    display := "flex",
    flexDirection := "column",
    gap := "24px",
    padding := "24px",
    maxWidth := "600px",
    margin := "0 auto",
    h1("Laminar Web Components Demo"),

    // HelloWorld with input
    div(
      display := "flex",
      flexDirection := "column",
      gap := "12px",
      h2("HelloWorld Component"),
      input(
        typ := "text",
        placeholder := "Enter your name",
        padding := "10px",
        fontSize := "16px",
        inContext { thisNode =>
          onInput.mapToValue --> { value =>
            thisNode.ref.parentElement
              .querySelector("hello-world")
              .setAttribute("name", value)
          }
        }
      ),
      HelloWorld(
        HelloWorld.name := "World"
      )
    ),

    // HelloCounter demos
    div(
      display := "flex",
      flexDirection := "column",
      gap := "12px",
      h2("HelloCounter Components"),
      div(
        display := "flex",
        gap := "16px",
        flexWrap := "wrap",
        HelloCounter(
          HelloCounter.label := "Score",
          HelloCounter.count := 0,
          HelloCounter.step := 10
        ),
        HelloCounter(
          HelloCounter.label := "Lives",
          HelloCounter.count := 3,
          HelloCounter.step := 1
        )
      )
    )
  )

  render(dom.document.getElementById("app"), app)
}
