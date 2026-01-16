import com.raquo.laminar.api.L.*

// =============================================================================
// TuIcon - Icon component using Material Symbols
// =============================================================================

object TuIcon extends LaminarWebComponent("tu-icon") {
  val iconName = attr.string("name", "star")
  val iconSize = attr.string("size", "24")
  val filled = attr.boolean("filled", false)

  override def styles: String = """
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    span {
      font-family: 'Material Symbols Outlined';
      font-weight: normal;
      font-style: normal;
      line-height: 1;
      letter-spacing: normal;
      text-transform: none;
      display: inline-block;
      white-space: nowrap;
      word-wrap: normal;
      direction: ltr;
      -webkit-font-smoothing: antialiased;
      font-variation-settings: 'FILL' var(--fill, 0);
    }
  """

  override def render: View = span(
    styleAttr <-- iconSize.signal.combineWith(filled.signal).map { (s, f) =>
      val fillValue = if (f) "1" else "0"
      s"font-size: ${s}px; --fill: $fillValue;"
    },
    child.text <-- iconName.signal
  )
}
