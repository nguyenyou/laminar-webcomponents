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

/** A string union attribute for type-safe string literal union values. */
class StringUnionAttr[T <: String](
    attrName: String,
    default: T,
    parse: String => T,
    codec: com.raquo.laminar.codecs.Codec[T, String],
    reflect: Boolean = false,
    val validValues: Set[String]
) extends ReactiveAttr[T](attrName, default, parse, codec, reflect)

object StringUnionAttr {

  /** Creates a StringUnionAttr for type-safe string literal union values. */
  inline def apply[T <: String](
      attrName: String,
      default: T,
      reflect: Boolean = false
  )(using uv: UnionValues[T]): StringUnionAttr[T] = {
    val validValues = uv.values
    val codec = new StringUnionCodec[T](validValues, default)
    new StringUnionAttr[T](
      attrName,
      default,
      parse = (s: String) => {
        if (validValues.contains(s)) s.asInstanceOf[T]
        else default
      },
      codec,
      reflect,
      validValues
    )
  }
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

/** An enum attribute for type-safe enum values. */
class EnumAttr[E](
    attrName: String,
    default: E,
    parse: String => E,
    codec: com.raquo.laminar.codecs.Codec[E, String],
    reflect: Boolean = false,
    val enumValues: Map[String, E]
) extends ReactiveAttr[E](attrName, default, parse, codec, reflect)

object EnumAttr {

  /** Creates an EnumAttr for type-safe enum values. */
  inline def apply[E](
      attrName: String,
      default: E,
      reflect: Boolean = false
  )(using ev: EnumValues[E]): EnumAttr[E] = {
    val enumVals = ev.values
    val codec = new EnumCodec[E](enumVals, default)
    new EnumAttr[E](
      attrName,
      default,
      parse = (s: String) => enumVals.getOrElse(s, default),
      codec,
      reflect,
      enumVals
    )
  }
}

/** Type class to extract values from an enum type */
trait EnumValues[E] {
  def values: Map[String, E]
}

object EnumValues {
  import scala.compiletime.*
  import scala.quoted.*
  import scala.deriving.Mirror

  class EnumValuesImpl[E](val values: Map[String, E]) extends EnumValues[E]

  inline given enumValues[E](using m: Mirror.SumOf[E]): EnumValues[E] =
    new EnumValuesImpl[E](extractEnumValues[E])

  inline def extractEnumValues[E](using m: Mirror.SumOf[E]): Map[String, E] = ${
    extractEnumValuesImpl[E]
  }

  def extractEnumValuesImpl[E: Type](using Quotes): Expr[Map[String, E]] = {
    import quotes.reflect.*

    val tpe = TypeRepr.of[E]
    val sym = tpe.typeSymbol

    if (!sym.flags.is(Flags.Enum)) {
      report.errorAndAbort(s"${sym.name} is not an enum type")
    }

    val cases = sym.children.filter(_.flags.is(Flags.Enum))

    val pairs: List[Expr[(String, E)]] = cases.map { caseSym =>
      val name = Expr(caseSym.name)
      val ref = Ref(caseSym).asExprOf[E]
      '{ ($name, $ref) }
    }

    '{ Map(${ Varargs(pairs) }*) }
  }
}

/** Codec for enum types */
class EnumCodec[E](values: Map[String, E], default: E)
    extends com.raquo.laminar.codecs.Codec[E, String] {
  private val reverseMap = values.map { case (k, v) => v -> k }
  override def decode(domValue: String): E = values.getOrElse(domValue, default)
  override def encode(scalaValue: E): String =
    reverseMap.getOrElse(scalaValue, reverseMap(default))
}

/** Wrapper for EnumAttr that allows defining IDE-friendly helper methods.
  * Usage:
  * ```scala
  * object variant
  *     extends EnumAttrDsl(attr.`enum`[Variant]("variant", Variant.Primary)) {
  *   lazy val Primary = attr := Variant.Primary
  *   lazy val Secondary = attr := Variant.Secondary
  * }
  * ```
  */
class EnumAttrDsl[E](val attr: EnumAttr[E]) extends ReactiveAttrDsl[E](attr)

/** Wrapper for StringUnionAttr that allows defining IDE-friendly helper
  * methods. Usage:
  * ```scala
  * object size
  *     extends StringUnionAttrDsl[Size](
  *       attr.stringUnion[Size]("size", "medium")
  *     ) {
  *   lazy val small = attr := "small"
  *   lazy val medium = attr := "medium"
  *   lazy val large = attr := "large"
  * }
  * ```
  */
class StringUnionAttrDsl[T <: String](val attr: StringUnionAttr[T])
    extends ReactiveAttrDsl[T](attr)

/** Base wrapper for ReactiveAttr that allows defining IDE-friendly helper
  * methods. Provides implicit conversion so the DSL object can be used wherever
  * ReactiveAttr is expected.
  */
class ReactiveAttrDsl[T](val _attr: ReactiveAttr[T]) {

  /** Access the underlying ReactiveAttr */
  def reactiveAttr: ReactiveAttr[T] = _attr

  /** Allow using := directly on the DSL object */
  def :=(value: T): Setter[HtmlElement] = _attr := value

  /** Allow using <-- for reactive bindings on the DSL object */
  def <--(source: Source[T]): Binder[HtmlElement] = _attr.asHtmlAttr <-- source

  /** Allow using apply syntax: _.size("small") */
  def apply(value: T): Setter[HtmlElement] = _attr := value
}

object ReactiveAttrDsl {

  /** Implicit conversion from DSL wrapper to ReactiveAttr for API compatibility
    */
  given [T]: Conversion[ReactiveAttrDsl[T], ReactiveAttr[T]] = _.reactiveAttr
}

object ReactiveAttr {
  def string(
      name: String,
      default: String = "",
      reflect: Boolean = false
  ): ReactiveAttr[String] =
    new ReactiveAttr(name, default, identity, StringAsIsCodec, reflect)

  /** Creates a type-safe string union attribute. */
  inline def stringUnion[T <: String](
      name: String,
      default: T,
      reflect: Boolean = false
  )(using uv: UnionValues[T]): StringUnionAttr[T] =
    StringUnionAttr[T](name, default, reflect)

  /** Creates a type-safe enum attribute. */
  inline def `enum`[E](
      name: String,
      default: E,
      reflect: Boolean = false
  )(using ev: EnumValues[E]): EnumAttr[E] =
    EnumAttr[E](name, default, reflect)

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
