import com.raquo.laminar.api.L.*
import CssMacro.css

object TuInput extends LaminarWebComponent("tu-input") {
  private val (_styles, classNames) = css"""
    :host {
      display: inline-block;
    }
    .input {
      width: 100%;
      box-sizing: border-box;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-family: system-ui, sans-serif;
      outline: none;
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
  """
  override def styles = _styles

  val inputValue = attr.string("value", "")
  val inputPlaceholder = attr.string("placeholder", "")
  val inputType = attr.string("type", "text")
  val inputDisabled = attr.boolean("disabled", false)
  val inputReadonly = attr.boolean("readonly", false)
  val inputSize = attr.string("size", "medium")

  override def render: View = input(
    cls <-- inputSize.signal.map(s => s"${classNames.input} $s"),
    typ <-- inputType.signal,
    placeholder <-- inputPlaceholder.signal,
    disabled <-- inputDisabled.signal,
    readOnly <-- inputReadonly.signal,
    controlled(
      value <-- inputValue.signal,
      onInput.mapToValue --> { v => inputValue.set(v) }
    )
  )
}
