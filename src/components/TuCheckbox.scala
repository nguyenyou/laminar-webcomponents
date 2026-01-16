import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*

// =============================================================================
// TuCheckbox - Checkbox component
// =============================================================================

object TuCheckbox extends LaminarWebComponent("tu-checkbox") {
  val isChecked = attr.boolean("checked", false)
  val isDisabled = attr.boolean("disabled", false)
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
    input[type="checkbox"] {
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
      typ := "checkbox",
      checked <-- isChecked.signal,
      disabled <-- isDisabled.signal,
      onChange.mapToChecked --> { c => isChecked.set(c) }
    ),
    span(child.text <-- labelText.signal),
    slotElement()
  )
}
