import com.raquo.laminar.api.L.*

// =============================================================================
// TuTextarea - Multiline text input component
// =============================================================================

object TuTextarea extends LaminarWebComponent("tu-textarea") {
  val textValue = attr.string("value", "")
  val textPlaceholder = attr.string("placeholder", "")
  val isDisabled = attr.boolean("disabled", false)
  val isReadonly = attr.boolean("readonly", false)
  val numRows = attr.int("rows", 3)
  val resizeMode =
    attr.string("resize", "vertical") // none, vertical, horizontal, both

  override def styles: String = """
    :host {
      display: block;
    }
    textarea {
      width: 100%;
      box-sizing: border-box;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      padding: 10px 14px;
      font-family: system-ui, sans-serif;
      font-size: 14px;
      outline: none;
      transition: border-color 0.15s, box-shadow 0.15s;
    }
    textarea:focus {
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    textarea:disabled {
      background: #f3f4f6;
      cursor: not-allowed;
    }
  """

  override def render: View = textArea(
    placeholder <-- textPlaceholder.signal,
    disabled <-- isDisabled.signal,
    readOnly <-- isReadonly.signal,
    rows <-- numRows.signal,
    styleAttr <-- resizeMode.signal.map(r => s"resize: $r;"),
    controlled(
      value <-- textValue.signal,
      onInput.mapToValue --> { v => textValue.set(v) }
    )
  )
}
