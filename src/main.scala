import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec
import org.scalajs.dom

// =============================================================================
// Main Application
// =============================================================================

@main
def main(): Unit = {
  // Define slot attribute for web component slots
  val slot = htmlAttr("slot", StringAsIsCodec)

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
      HelloWorld(HelloWorld.name := "World")
    ),

    // HelloCounter demos
    div(
      h2("HelloCounter Components"),
      div(
        display := "flex",
        gap := "16px",
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
    ),

    // TuButton demos
    div(
      h2("TuButton Component"),
      div(
        display := "flex",
        gap := "12px",
        flexWrap := "wrap",
        alignItems := "center",
        TuButton(TuButton.variant := "primary", "Primary"),
        TuButton(TuButton.variant := "secondary", "Secondary"),
        TuButton(TuButton.variant := "outline", "Outline"),
        TuButton(TuButton.variant := "ghost", "Ghost"),
        TuButton(TuButton.btnDisabled := true, "Disabled")
      ),
      div(
        marginTop := "12px",
        display := "flex",
        gap := "12px",
        alignItems := "center",
        TuButton(TuButton.btnSize := "small", "Small"),
        TuButton(TuButton.btnSize := "medium", "Medium"),
        TuButton(TuButton.btnSize := "large", "Large")
      )
    ),

    // TuIcon demos
    div(
      h2("TuIcon Component"),
      div(
        display := "flex",
        gap := "16px",
        alignItems := "center",
        TuIcon(TuIcon.iconName := "home"),
        TuIcon(TuIcon.iconName := "settings"),
        TuIcon(TuIcon.iconName := "favorite", TuIcon.filled := true),
        TuIcon(TuIcon.iconName := "star", TuIcon.iconSize := "32"),
        TuIcon(
          TuIcon.iconName := "check_circle",
          TuIcon.iconSize := "48",
          TuIcon.filled := true
        )
      )
    ),

    // TuButton with TuIcon (slots)
    div(
      h2("TuButton with Icons (Slots)"),
      div(
        display := "flex",
        gap := "12px",
        alignItems := "center",
        TuButton(
          TuIcon(TuIcon.iconName := "add", slot := "prefix"),
          "Add Item"
        ),
        TuButton(
          TuButton.variant := "outline",
          "Download",
          TuIcon(TuIcon.iconName := "download", slot := "suffix")
        ),
        TuButton(
          TuButton.variant := "secondary",
          TuIcon(TuIcon.iconName := "arrow_back", slot := "prefix"),
          "Back",
          TuIcon(TuIcon.iconName := "arrow_forward", slot := "suffix")
        )
      )
    ),

    // TuInput demos
    div(
      h2("TuInput Component"),
      div(
        display := "flex",
        flexDirection := "column",
        gap := "12px",
        TuInput(TuInput.inputPlaceholder := "Enter text..."),
        TuInput(
          TuInput.inputType := "password",
          TuInput.inputPlaceholder := "Password"
        ),
        TuInput(
          TuInput.inputSize := "small",
          TuInput.inputPlaceholder := "Small input"
        ),
        TuInput(
          TuInput.inputSize := "large",
          TuInput.inputPlaceholder := "Large input"
        ),
        TuInput(
          TuInput.inputDisabled := true,
          TuInput.inputValue := "Disabled input"
        )
      )
    ),

    // TuCheckbox demos
    div(
      h2("TuCheckbox Component"),
      div(
        display := "flex",
        flexDirection := "column",
        gap := "8px",
        TuCheckbox(TuCheckbox.labelText := "Option 1"),
        TuCheckbox(
          TuCheckbox.labelText := "Option 2",
          TuCheckbox.isChecked := true
        ),
        TuCheckbox(
          TuCheckbox.labelText := "Disabled",
          TuCheckbox.isDisabled := true
        )
      )
    ),

    // TuRadio demos
    div(
      h2("TuRadio Component"),
      div(
        display := "flex",
        flexDirection := "column",
        gap := "8px",
        TuRadio(
          TuRadio.radioName := "choice",
          TuRadio.radioValue := "a",
          TuRadio.labelText := "Choice A"
        ),
        TuRadio(
          TuRadio.radioName := "choice",
          TuRadio.radioValue := "b",
          TuRadio.labelText := "Choice B"
        ),
        TuRadio(
          TuRadio.radioName := "choice",
          TuRadio.radioValue := "c",
          TuRadio.labelText := "Choice C",
          TuRadio.isChecked := true
        )
      )
    ),

    // TuTextarea demos
    div(
      h2("TuTextarea Component"),
      TuTextarea(
        TuTextarea.textPlaceholder := "Enter your message...",
        TuTextarea.numRows := 4
      )
    ),

    // TuSelect demos
    div(
      h2("TuSelect Component"),
      TuSelect(
        TuSelect.selectPlaceholder := "Choose a fruit",
        TuOption(
          TuOption.optionValue := "apple",
          TuOption.optionLabel := "Apple"
        ),
        TuOption(
          TuOption.optionValue := "banana",
          TuOption.optionLabel := "Banana"
        ),
        TuOption(
          TuOption.optionValue := "cherry",
          TuOption.optionLabel := "Cherry"
        )
      )
    )
  )

  render(dom.document.getElementById("app"), app)
}
