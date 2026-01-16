import scala.scalajs.js
import org.scalajs.dom
import com.raquo.laminar.api.L.*
import com.raquo.laminar.tags.CustomHtmlTag
import com.raquo.laminar.codecs.StringAsIsCodec

// =============================================================================
// Reactive Attribute - Definition class (used in attrs object)
// =============================================================================

class ReactiveAttr[T](
    val attrName: String,
    val default: T,
    val parse: String => T,
    val codec: com.raquo.laminar.codecs.Codec[T, String]
) {
  def asHtmlAttr: HtmlAttr[T] = htmlAttr(attrName, codec)

  // Convenience operator for setting attribute values directly
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

// =============================================================================
// Macros
// =============================================================================

/** Extracts all ReactiveAttr field names from an object type */
inline def extractObservedAttributes[T]: js.Array[String] =
  ${ extractObservedAttributesImpl[T] }

private def extractObservedAttributesImpl[T: scala.quoted.Type](using
    q: scala.quoted.Quotes
): scala.quoted.Expr[js.Array[String]] = {
  import q.reflect.*
  import scala.quoted.{Expr, Varargs}

  val tpe = TypeRepr.of[T]
  val moduleSymbol = tpe.typeSymbol
  val reactiveAttrType = TypeRepr.of[ReactiveAttr[?]]

  val attrFields = moduleSymbol.fieldMembers.filter { field =>
    val fieldType = tpe.memberType(field)
    fieldType <:< reactiveAttrType
  }

  val attrNames: List[Expr[String]] = attrFields.map { field =>
    def extractStringLiteral(tree: Tree): Option[String] = tree match {
      case Literal(StringConstant(s))      => Some(s)
      case Apply(_, args) if args.nonEmpty => extractStringLiteral(args.head)
      case _                               => None
    }

    field.tree match {
      case ValDef(_, _, Some(rhs)) =>
        extractStringLiteral(rhs).map(Expr(_)).getOrElse {
          Expr(field.name.stripSuffix(" "))
        }
      case _ =>
        Expr(field.name.stripSuffix(" "))
    }
  }

  '{ js.Array(${ Varargs(attrNames) }*) }
}

inline def registerWebComponent[A](
    tagName: String,
    ctor: js.Dynamic
): WebComponentApi = {
  val attrNames = extractObservedAttributes[A]
  ctor.observedAttributes = attrNames
  dom.window.customElements.define(tagName, ctor)
  new WebComponentApi(tagName)
}

// =============================================================================
// WebComponentApi - Returned by registerWebComponent macro
// =============================================================================

class WebComponentApi(val tagName: String) {
  lazy val tag: CustomHtmlTag[dom.HTMLElement] = CustomHtmlTag(tagName)

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = tag(mods*)

  def stringAttr(name: String): HtmlAttr[String] =
    htmlAttr(name, StringAsIsCodec)

  def intAttr(name: String): HtmlAttr[Int] =
    htmlAttr(name, com.raquo.laminar.codecs.IntAsStringCodec)

  def doubleAttr(name: String): HtmlAttr[Double] =
    htmlAttr(name, com.raquo.laminar.codecs.DoubleAsStringCodec)

  def boolAttr(name: String): HtmlAttr[Boolean] =
    htmlAttr(name, com.raquo.laminar.codecs.BooleanAsTrueFalseStringCodec)
}
