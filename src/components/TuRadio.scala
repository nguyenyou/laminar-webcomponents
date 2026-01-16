import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import CssMacro.css

object TuRadio extends LaminarWebComponent("tu-radio") {
  type Self = TuRadio.type
  private val (_styles, classNames) = css"""
    :host {
      display: inline-block;
    }
    .label {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      font-family: system-ui, sans-serif;
      font-size: 14px;
      color: #374151;

      &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
    .radio {
      width: 18px;
      height: 18px;
      margin: 0;
      cursor: pointer;
      accent-color: #3b82f6;

      &:disabled {
        cursor: not-allowed;
      }
    }
  """
  override def styles = _styles

  val isChecked = attr.boolean("checked", false)
  val isDisabled = attr.boolean("disabled", false)
  val radioName = attr.string("name", "")
  val radioValue = attr.string("value", "")
  val labelText = attr.string("label", "")

  override def render: View = L.label(
    cls <-- isDisabled.signal.map(d =>
      if (d) s"${classNames.label} ${classNames.disabled}" else classNames.label
    ),
    input(
      cls := classNames.radio,
      typ := "radio",
      nameAttr <-- radioName.signal,
      value <-- radioValue.signal,
      checked <-- isChecked.signal,
      disabled <-- isDisabled.signal,
      onChange.mapToChecked --> { ch => isChecked.set(ch) }
    ),
    span(child.text <-- labelText.signal),
    slotElement()
  )
}
