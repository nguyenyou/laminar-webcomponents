import com.raquo.laminar.api.L.*
import scala.scalajs.js

trait LaminarWebComponent[C <: WebComponent] {
  def tagName: String

  // This will be implemented by the user, or we can use macros to define it
  // For now, let's assume the user defines it.

  protected def register(ctor: js.Dynamic): WebComponentApi = {
    registerWebComponent[this.type](tagName, ctor)
  }

  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    htmlTag(tagName)(mods*)

  protected object attr {
    def string(name: String, default: String = "") =
      ReactiveAttr.string(name, default)
    def int(name: String, default: Int = 0) = ReactiveAttr.int(name, default)
    def boolean(name: String, default: Boolean = false) =
      ReactiveAttr.boolean(name, default)
    def double(name: String, default: Double = 0.0) =
      ReactiveAttr.double(name, default)
  }
}
