import scala.quoted.*
import scala.scalajs.js

// =============================================================================
// Reactive Attribute - Runtime class
// =============================================================================

class ReactiveAttr[T](
    val attrName: String,
    val default: T,
    val parse: String => T
)

object ReactiveAttr {
  def string(name: String, default: String = ""): ReactiveAttr[String] =
    new ReactiveAttr(name, default, identity)

  def int(name: String, default: Int = 0): ReactiveAttr[Int] =
    new ReactiveAttr(name, default, s => s.toIntOption.getOrElse(default))

  def boolean(name: String, default: Boolean = false): ReactiveAttr[Boolean] =
    new ReactiveAttr(name, default, _ != null)

  def double(name: String, default: Double = 0.0): ReactiveAttr[Double] =
    new ReactiveAttr(name, default, s => s.toDoubleOption.getOrElse(default))
}

// =============================================================================
// Macros for extracting attribute names from companion objects
// =============================================================================

/** Extracts all ReactiveAttr field names from a companion object type */
inline def extractObservedAttributes[T]: js.Array[String] =
  ${ extractObservedAttributesImpl[T] }

private def extractObservedAttributesImpl[T: Type](using
    Quotes
): Expr[js.Array[String]] = {
  import quotes.reflect.*

  val tpe = TypeRepr.of[T]
  val moduleSymbol = tpe.typeSymbol

  // Find all vals that return ReactiveAttr[_]
  val reactiveAttrType = TypeRepr.of[ReactiveAttr[?]]

  val attrFields = moduleSymbol.fieldMembers.filter { field =>
    val fieldType = tpe.memberType(field)
    fieldType <:< reactiveAttrType
  }

  // For each field, extract the attrName from the initializer
  // Handles patterns like: ReactiveAttr.string("name", "default")
  val attrNames: List[Expr[String]] = attrFields.map { field =>
    // Try to extract literal string from the AST
    def extractStringLiteral(tree: Tree): Option[String] = tree match {
      case Literal(StringConstant(s))      => Some(s)
      case Apply(_, args) if args.nonEmpty => extractStringLiteral(args.head)
      case _                               => None
    }

    field.tree match {
      case ValDef(_, _, Some(rhs)) =>
        extractStringLiteral(rhs).map(Expr(_)).getOrElse {
          // Fallback: use field name as attribute name (kebab-case convention)
          Expr(field.name.stripSuffix(" "))
        }
      case _ =>
        Expr(field.name.stripSuffix(" "))
    }
  }

  '{ js.Array(${ Varargs(attrNames) }*) }
}
