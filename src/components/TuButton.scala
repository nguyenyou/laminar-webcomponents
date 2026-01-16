import com.raquo.laminar.api.L.*
import CssMacro.css

// =============================================================================
// TuButton - Button component with slot support for icons
// =============================================================================

object TuButton extends LaminarWebComponent("tu-button") {
  private val (_styles, classNames) = css"""
    :host {
      display: inline-block;
    }
    .btn {
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

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      &.small { padding: 6px 12px; font-size: 13px; }
      &.medium { padding: 10px 18px; font-size: 14px; }
      &.large { padding: 14px 24px; font-size: 16px; }

      &.primary {
        background: #3b82f6;
        color: white;
        &:hover:not(:disabled) { background: #2563eb; }
      }

      &.secondary {
        background: #6b7280;
        color: white;
        &:hover:not(:disabled) { background: #4b5563; }
      }

      &.outline {
        background: transparent;
        color: #3b82f6;
        border: 1px solid #3b82f6;
        &:hover:not(:disabled) { background: #eff6ff; }
      }

      &.ghost {
        background: transparent;
        color: #374151;
        &:hover:not(:disabled) { background: #f3f4f6; }
      }
    }
    ::slotted(tu-icon) {
      display: inline-flex;
    }
  """
  override def styles = _styles

  val variant =
    attr.string("variant", "primary") // primary, secondary, outline, ghost
  val btnSize = attr.string("size", "medium") // small, medium, large
  val btnDisabled = attr.boolean("disabled", false)

  override def render: View = button(
    cls <-- variant.signal
      .combineWith(btnSize.signal)
      .map((v, s) => s"${classNames.btn} $v $s"),
    disabled <-- btnDisabled.signal,
    slotElement("prefix"),
    slotElement(),
    slotElement("suffix")
  )
}
