import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import CssMacro.css

// =============================================================================
// TuSelect - Select dropdown component with slot for tu-option
// =============================================================================

object TuSelect extends LaminarWebComponent("tu-select") {
  private val (_styles, classNames) = css"""
    :host {
      display: inline-block;
    }
    .wrapper {
      position: relative;
      display: inline-block;
      width: 100%;
    }
    .select {
      width: 100%;
      box-sizing: border-box;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-family: system-ui, sans-serif;
      background: white;
      cursor: pointer;
      outline: none;
      appearance: none;
      padding-right: 32px;
      transition: border-color 0.15s, box-shadow 0.15s;

      &:focus {
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
      }

      &:disabled {
        background: #f3f4f6;
        cursor: not-allowed;
      }

      &.small { padding: 6px 10px; font-size: 13px; }
      &.medium { padding: 10px 14px; font-size: 14px; }
      &.large { padding: 14px 18px; font-size: 16px; }
    }
    .arrow {
      position: absolute;
      right: 12px;
      top: 50%;
      transform: translateY(-50%);
      pointer-events: none;
      color: #6b7280;
    }
    ::slotted(tu-option) {
      display: none;
    }
  """
  override def styles = _styles

  val selectValue = attr.string("value", "")
  val selectPlaceholder = attr.string("placeholder", "Select an option")
  val isDisabled = attr.boolean("disabled", false)
  val selectSize = attr.string("size", "medium") // small, medium, large

  override def render: View = div(
    cls := classNames.wrapper,
    L.select(
      cls <-- selectSize.signal.map(s => s"${classNames.select} $s"),
      disabled <-- isDisabled.signal,
      value <-- selectValue.signal,
      onChange.mapToValue --> { v => selectValue.set(v) },
      option(
        value := "",
        disabled := true,
        child.text <-- selectPlaceholder.signal
      )
    ),
    span(cls := classNames.arrow, "▼"),
    slotElement()
  )
}

// =============================================================================
// TuOption - Option for TuSelect
// =============================================================================

object TuOption extends LaminarWebComponent("tu-option") {
  private val (_styles, classNames) = css"""
    :host {
      display: none;
    }
    .option {
      display: none;
    }
  """
  override def styles = _styles

  val optionValue = attr.string("value", "")
  val optionLabel = attr.string("label", "")
  val isDisabled = attr.boolean("disabled", false)

  override def render: View = span(
    cls := classNames.option,
    dataAttr("value") <-- optionValue.signal,
    dataAttr("label") <-- optionLabel.signal
  )
}
