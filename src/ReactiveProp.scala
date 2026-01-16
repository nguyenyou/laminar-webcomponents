import com.raquo.laminar.api.L.*
import org.scalajs.dom

class ReactiveProp[T](element: dom.HTMLElement, attr: ReactiveAttr[T]) {
  private val _var = Var(attr.default)
  private var _isReflecting = false

  val signal: Signal[T] = _var.signal
  def get: T = _var.now()

  def set(value: T): Unit = {
    _var.set(value)
    reflectToAttribute(value)
  }

  def update(f: T => T): Unit = {
    _var.update { old =>
      val next = f(old)
      reflectToAttribute(next)
      next
    }
  }

  def handleChange(newValue: String | Null): Unit = {
    // Skip if this change was triggered by our own reflection
    if (!_isReflecting) {
      val nextValue = Option(newValue).map(attr.parse).getOrElse(attr.default)
      _var.set(nextValue)
    }
  }

  private def reflectToAttribute(value: T): Unit = {
    if (attr.reflect) {
      val encoded = attr.codec.encode(value)
      _isReflecting = true
      try {
        if (encoded == null) {
          element.removeAttribute(attr.attrName)
        } else {
          element.setAttribute(attr.attrName, encoded)
        }
      } finally {
        _isReflecting = false
      }
    }
  }
}
