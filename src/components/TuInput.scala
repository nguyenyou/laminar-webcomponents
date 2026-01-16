import com.raquo.laminar.api.L.*

// =============================================================================
// TuInput - Text input component
// =============================================================================

object TuInput extends LaminarWebComponent("tu-input") {
  val inputValue = attr.string("value", "")
  val inputPlaceholder = attr.string("placeholder", "")
  val inputType = attr.string("type", "text")
  val inputDisabled = attr.boolean("disabled", false)
  val inputReadonly = attr.boolean("readonly", false)
  val inputSize = attr.string("size", "medium") // small, medium, large

  override def styles: String = """
    :host {
      display: inline-block;
    }
    input {
      width: 100%;
      box-sizing: border-box;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-family: system-ui, sans-serif;
      outline: none;
      transition: border-color 0.15s, box-shadow 0.15s;
    }
    input:focus {
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    input:disabled {
      background: #f3f4f6;
      cursor: not-allowed;
    }
    input.small { padding: 6px 10px; font-size: 13px; }
    input.medium { padding: 10px 14px; font-size: 14px; }
    input.large { padding: 14px 18px; font-size: 16px; }
  """

  override def render: View = input(
    cls <-- inputSize.signal,
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
