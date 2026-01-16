import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

class ReactiveAttr[T](
    val attrName: String,
    val default: T,
    val parse: String => T,
    val codec: com.raquo.laminar.codecs.Codec[T, String]
) {
  def asHtmlAttr: HtmlAttr[T] = htmlAttr(attrName, codec)

  def :=(value: T): Setter[HtmlElement] = asHtmlAttr := value
}

object ReactiveAttr {
  def string(name: String, default: String = ""): ReactiveAttr[String] =
    new ReactiveAttr(name, default, identity, StringAsIsCodec)

  def int(name: String, default: Int = 0): ReactiveAttr[Int] =
    new ReactiveAttr(
      name,
      default,
      s => s.toIntOption.getOrElse(default),
      com.raquo.laminar.codecs.IntAsStringCodec
    )

  def boolean(name: String, default: Boolean = false): ReactiveAttr[Boolean] =
    new ReactiveAttr(
      name,
      default,
      _ != null,
      com.raquo.laminar.codecs.BooleanAsTrueFalseStringCodec
    )

  def double(name: String, default: Double = 0.0): ReactiveAttr[Double] =
    new ReactiveAttr(
      name,
      default,
      s => s.toDoubleOption.getOrElse(default),
      com.raquo.laminar.codecs.DoubleAsStringCodec
    )
}
