import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

class ReactiveAttr[T](
    val attrName: String,
    val default: T,
    val parse: String => T,
    val codec: com.raquo.laminar.codecs.Codec[T, String],
    val reflect: Boolean = false
) {
  def asHtmlAttr: HtmlAttr[T] = htmlAttr(attrName, codec)

  def :=(value: T): Setter[HtmlElement] = asHtmlAttr := value
}

/** Type class to extract valid values from a union type */
trait UnionValues[T] {
  def values: Set[String]
}

object UnionValues {
  import scala.compiletime.*
  import scala.quoted.*

  // Note: must be accessible for inline expansion
  class UnionValuesImpl[T](val values: Set[String]) extends UnionValues[T]

  inline given unionValues[T]: UnionValues[T] =
    new UnionValuesImpl[T](extractValues[T])

  inline def extractValues[T]: Set[String] = ${ extractValuesImpl[T] }

  def extractValuesImpl[T: Type](using Quotes): Expr[Set[String]] = {
    import quotes.reflect.*

    def collectLiterals(tpe: TypeRepr): List[String] = {
      tpe.dealias match {
        case ConstantType(StringConstant(s)) => List(s)
        case OrType(left, right) =>
          collectLiterals(left) ++ collectLiterals(right)
        case other =>
          report.errorAndAbort(
            s"Expected string literal types in union, got: ${other.show}"
          )
      }
    }

    val literals = collectLiterals(TypeRepr.of[T])
    Expr(literals.toSet)
  }
}

/** Codec for string union types */
class StringUnionCodec[T](values: Set[String], default: T)
    extends com.raquo.laminar.codecs.Codec[T, String] {
  override def decode(domValue: String): T = domValue.asInstanceOf[T]
  override def encode(scalaValue: T): String = scalaValue.asInstanceOf[String]
}

object ReactiveAttr {
  def string(
      name: String,
      default: String = "",
      reflect: Boolean = false
  ): ReactiveAttr[String] =
    new ReactiveAttr(name, default, identity, StringAsIsCodec, reflect)

  /** Creates a type-safe string union attribute.
    *
    * Usage:
    * ```scala
    * type Variant = "primary" | "secondary" | "outline"
    * val variant = attr.stringUnion[Variant]("variant", "primary")
    * ```
    */
  inline def stringUnion[T <: String](
      name: String,
      default: T,
      reflect: Boolean = false
  )(using uv: UnionValues[T]): ReactiveAttr[T] = {
    val validValues = uv.values
    val codec = new StringUnionCodec[T](validValues, default)
    new ReactiveAttr[T](
      name,
      default,
      parse = (s: String) => {
        if (validValues.contains(s)) s.asInstanceOf[T]
        else default
      },
      codec,
      reflect
    )
  }

  def int(
      name: String,
      default: Int = 0,
      reflect: Boolean = false
  ): ReactiveAttr[Int] =
    new ReactiveAttr(
      name,
      default,
      s => s.toIntOption.getOrElse(default),
      com.raquo.laminar.codecs.IntAsStringCodec,
      reflect
    )

  def boolean(
      name: String,
      default: Boolean = false,
      reflect: Boolean = false
  ): ReactiveAttr[Boolean] =
    new ReactiveAttr(
      name,
      default,
      _ != null,
      com.raquo.laminar.codecs.BooleanAsTrueFalseStringCodec,
      reflect
    )

  def double(
      name: String,
      default: Double = 0.0,
      reflect: Boolean = false
  ): ReactiveAttr[Double] =
    new ReactiveAttr(
      name,
      default,
      s => s.toDoubleOption.getOrElse(default),
      com.raquo.laminar.codecs.DoubleAsStringCodec,
      reflect
    )
}
