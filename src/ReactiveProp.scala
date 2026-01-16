import com.raquo.laminar.api.L.*

class ReactiveProp[T](attr: ReactiveAttr[T]) {
  private val _var = Var(attr.default)
  val signal: Signal[T] = _var.signal
  def get: T = _var.now()
  def set(value: T): Unit = _var.set(value)

  def handleChange(newValue: String | Null): Unit = {
    _var.set(Option(newValue).map(attr.parse).getOrElse(attr.default))
  }
}
