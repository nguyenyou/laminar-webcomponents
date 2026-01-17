import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom

@main
def main(): Unit = {
  val slot = htmlAttr("slot", StringAsIsCodec)

  val app = div(
    display := "flex",
    flexDirection := "column",
    gap := "24px",
    padding := "24px",
    maxWidth := "600px",
    margin := "0 auto",
    h1("Laminar Web Components Demo"),
    div(
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
      HelloWorld(_.name := "World")()
    ),
    div(
      h2("HelloCounter Components"),
      div(
        display := "flex",
        gap := "16px",
        HelloCounter(
          _.label := "Score",
          _.count := 0,
          _.step := 10
        )(),
        HelloCounter(
          _.label := "Lives",
          _.count := 3,
          _.step := 1
        )()
      )
    ),
    div(
      h2("TuButton Component"),
      div(
        display := "flex",
        gap := "12px",
        flexWrap := "wrap",
        alignItems := "center",
        TuButton(_.variant := "primary")("Primary"),
        TuButton(_.variant := "secondary")("Secondary"),
        TuButton(_.variant := "outline")("Outline"),
        TuButton(_.variant := "ghost")("Ghost"),
        TuButton(_.btnDisabled := true)("Disabled")
      ),
      div(
        marginTop := "12px",
        display := "flex",
        gap := "12px",
        alignItems := "center",
        TuButton(_.btnSize := "small")("Small"),
        TuButton(_.btnSize := "medium")("Medium"),
        TuButton(_.btnSize := "large")("Large")
      )
    ),
    div(
      h2("TuIcon Component"),
      div(
        display := "flex",
        gap := "16px",
        alignItems := "center",
        TuIcon(_.iconName := "home")(),
        TuIcon(_.iconName := "settings")(),
        TuIcon(_.iconName := "favorite", _.filled := true)(),
        TuIcon(_.iconName := "star", _.iconSize := "32")(),
        TuIcon(
          _.iconName := "check_circle",
          _.iconSize := "48",
          _.filled := true
        )()
      )
    ),
    div(
      h2("TuButton with Icons (Slots)"),
      div(
        display := "flex",
        gap := "12px",
        alignItems := "center",
        TuButton(
          _.slots.prefix(TuIcon(_.iconName := "add")())
        )("Add Item"),
        TuButton(
          _.variant := "outline",
          _.slots.suffix(TuIcon(_.iconName := "download")())
        )("Download"),
        TuButton(
          _.variant := "secondary",
          _.slots.prefix(TuIcon(_.iconName := "arrow_back")()),
          _.slots.suffix(TuIcon(_.iconName := "arrow_forward")())
        )("Back")
      )
    ),
    div(
      h2("TuInput Component"),
      div(
        display := "flex",
        flexDirection := "column",
        gap := "12px",
        TuInput(_.inputPlaceholder := "Enter text...")(),
        TuInput(
          _.inputType := "password",
          _.inputPlaceholder := "Password"
        )(),
        TuInput(
          _.inputSize := "small",
          _.inputPlaceholder := "Small input"
        )(),
        TuInput(
          _.inputSize := "large",
          _.inputPlaceholder := "Large input"
        )(),
        TuInput(
          _.inputDisabled := true,
          _.inputValue := "Disabled input"
        )()
      )
    ),
    div(
      h2("TuCheckbox Component"),
      div(
        display := "flex",
        flexDirection := "column",
        gap := "8px",
        TuCheckbox(_.labelText := "Option 1")(),
        TuCheckbox(
          _.labelText := "Option 2",
          _.isChecked := true
        )(),
        TuCheckbox(
          _.labelText := "Disabled",
          _.isDisabled := true
        )()
      )
    ),
    div(
      h2("TuRadio Component"),
      div(
        display := "flex",
        flexDirection := "column",
        gap := "8px",
        TuRadio(
          _.radioName := "choice",
          _.radioValue := "a",
          _.labelText := "Choice A"
        )(),
        TuRadio(
          _.radioName := "choice",
          _.radioValue := "b",
          _.labelText := "Choice B"
        )(),
        TuRadio(
          _.radioName := "choice",
          _.radioValue := "c",
          _.labelText := "Choice C",
          _.isChecked := true
        )()
      )
    ),
    div(
      h2("TuTextarea Component"),
      TuTextarea(
        _.textPlaceholder := "Enter your message...",
        _.numRows := 4
      )()
    ),
    div(
      h2("TuSelect Component"),
      TuSelect(_.selectPlaceholder := "Choose a fruit")(
        TuOption(_.optionValue := "apple", _.optionLabel := "Apple")(),
        TuOption(_.optionValue := "banana", _.optionLabel := "Banana")(),
        TuOption(_.optionValue := "cherry", _.optionLabel := "Cherry")()
      )
    )
  )

  render(dom.document.getElementById("app"), app)
}
