import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*

// =============================================================================
// TuRadio - Radio button component
// =============================================================================

object TuRadio extends LaminarWebComponent("tu-radio") {
  val isChecked = attr.boolean("checked", false)
  val isDisabled = attr.boolean("disabled", false)
  val radioName = attr.string("name", "")
  val radioValue = attr.string("value", "")
  val labelText = attr.string("label", "")

  override def styles: String = """
    :host {
      display: inline-block;
    }
    label {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      font-family: system-ui, sans-serif;
      font-size: 14px;
      color: #374151;
    }
    label.disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    input[type="radio"] {
      width: 18px;
      height: 18px;
      margin: 0;
      cursor: pointer;
      accent-color: #3b82f6;
    }
    input:disabled {
      cursor: not-allowed;
    }
  """

  override def render: View = L.label(
    cls <-- isDisabled.signal.map(d => if (d) "disabled" else ""),
    input(
      typ := "radio",
      nameAttr <-- radioName.signal,
      value <-- radioValue.signal,
      checked <-- isChecked.signal,
      disabled <-- isDisabled.signal,
      onChange.mapToChecked --> { c => isChecked.set(c) }
    ),
    span(child.text <-- labelText.signal),
    slotElement()
  )
}
