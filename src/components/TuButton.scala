import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.Slot
import org.scalajs.dom
import scala.scalajs.js

object TuButton extends LaminarWebComponent("tu-button") {

  // Typed detail for the hovered event
  trait HoveredDetail extends js.Object {
    val isHovered: Boolean
  }
  object HoveredDetail {
    def apply(isHovered: Boolean): HoveredDetail =
      js.Dynamic.literal(isHovered = isHovered).asInstanceOf[HoveredDetail]
  }
  // Enum with different name to avoid Scala.js linker conflict with `variant` object
  enum ButtonVariant {
    case Primary, Secondary, Outline, Ghost

    def cssClass: String = this match {
      case Primary   => "primary"
      case Secondary => "secondary"
      case Outline   => "outline"
      case Ghost     => "ghost"
    }
  }

  type ButtonSize = "small" | "medium" | "large"

  // Public API: _.variant.Primary, _.variant.Secondary, etc.
  object variant
      extends EnumAttrDsl(
        attr.`enum`[ButtonVariant]("variant", ButtonVariant.Primary)
      ) {
    def Primary: Setter[HtmlElement] = attr := ButtonVariant.Primary
    def Secondary: Setter[HtmlElement] = attr := ButtonVariant.Secondary
    def Outline: Setter[HtmlElement] = attr := ButtonVariant.Outline
    def Ghost: Setter[HtmlElement] = attr := ButtonVariant.Ghost
  }

  // Public API: _.size.small, _.size.medium, _.size.large
  object size
      extends StringUnionAttrDsl[ButtonSize](
        attr.stringUnion[ButtonSize]("size", "medium")
      ) {
    def small: Setter[HtmlElement] = attr := "small"
    def medium: Setter[HtmlElement] = attr := "medium"
    def large: Setter[HtmlElement] = attr := "large"
  }

  val disabled = attr.boolean("disabled", false)

  val onClick = com.raquo.laminar.api.L.onClick

  // Custom event: dispatched when the button is hovered/unhovered
  val onHovered = new CustomEventProp[HoveredDetail]("hovered")

  object slots {
    val prefix: Slot = Slot("prefix")
    val suffix: Slot = Slot("suffix")
  }

  override def render: View = button(
    cls <-- variant.signal
      .combineWith(size.signal)
      .map((v, s) => s"${classNames.btn} ${v.cssClass} $s"),
    com.raquo.laminar.api.L.disabled <-- disabled.signal,
    onMouseEnter --> { _ =>
      dispatchCustomEvent("hovered", HoveredDetail(true))
    },
    onMouseLeave --> { _ =>
      dispatchCustomEvent("hovered", HoveredDetail(false))
    },
    slotElement("prefix"),
    slotElement(),
    slotElement("suffix")
  )

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

}
