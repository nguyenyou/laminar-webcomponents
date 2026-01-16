import com.raquo.laminar.api.L.*

// =============================================================================
// TuButton - Button component with slot support for icons
// =============================================================================

object TuButton extends LaminarWebComponent("tu-button") {
  val variant =
    attr.string("variant", "primary") // primary, secondary, outline, ghost
  val btnSize = attr.string("size", "medium") // small, medium, large
  val btnDisabled = attr.boolean("disabled", false)

  override def styles: String = """
    :host {
      display: inline-block;
    }
    button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      border: none;
      border-radius: 6px;
      font-family: system-ui, sans-serif;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.15s ease;
    }
    button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    /* Sizes */
    button.small { padding: 6px 12px; font-size: 13px; }
    button.medium { padding: 10px 18px; font-size: 14px; }
    button.large { padding: 14px 24px; font-size: 16px; }
    /* Variants */
    button.primary {
      background: #3b82f6;
      color: white;
    }
    button.primary:hover:not(:disabled) { background: #2563eb; }
    button.secondary {
      background: #6b7280;
      color: white;
    }
    button.secondary:hover:not(:disabled) { background: #4b5563; }
    button.outline {
      background: transparent;
      color: #3b82f6;
      border: 1px solid #3b82f6;
    }
    button.outline:hover:not(:disabled) { background: #eff6ff; }
    button.ghost {
      background: transparent;
      color: #374151;
    }
    button.ghost:hover:not(:disabled) { background: #f3f4f6; }
    /* Slots */
    ::slotted(tu-icon) {
      display: inline-flex;
    }
  """

  override def render: View = button(
    cls <-- variant.signal.combineWith(btnSize.signal).map((v, s) => s"$v $s"),
    disabled <-- btnDisabled.signal,
    slotElement("prefix"),
    slotElement(),
    slotElement("suffix")
  )
}
