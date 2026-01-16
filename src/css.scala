import scala.quoted.*
import scala.collection.mutable

object CssFlattener {
  def flatten(css: String): String = {
    val rules = parseBlock(css, "")
    val sb = new StringBuilder
    renderRules(rules, sb)
    sb.toString
  }

  private case class Rule(
      selector: String,
      properties: List[String],
      children: List[Rule]
  )

  private def skipString(content: String, start: Int, quote: Char): Int = {
    var i = start + 1
    while (i < content.length) {
      val c = content.charAt(i)
      if (c == quote && content.charAt(i - 1) != '\\') {
        return i + 1
      }
      i += 1
    }
    i
  }

  private def parseBlock(
      content: String,
      parentSelector: String
  ): List[Rule] = {
    val rules = mutable.ListBuffer[Rule]()
    var i = 0
    val len = content.length

    while (i < len) {
      while (i < len && content.charAt(i).isWhitespace) i += 1
      if (i >= len) return rules.toList

      val selectorStart = i
      while (i < len) {
        val c = content.charAt(i)
        if (c == '"' || c == '\'') {
          i = skipString(content, i, c)
        } else if (c == '{' || c == '}') {
          i = i
          return if (c == '}') rules.toList
          else {
            val selector = content.substring(selectorStart, i).trim
            if (selector.isEmpty) {
              i += 1
              rules.toList
            } else {
              i += 1

              val bodyStart = i
              var depth = 1
              while (i < len && depth > 0) {
                val bc = content.charAt(i)
                if (bc == '"' || bc == '\'') {
                  i = skipString(content, i, bc)
                } else {
                  bc match {
                    case '{' => depth += 1; i += 1
                    case '}' => depth -= 1; if (depth > 0) i += 1
                    case _   => i += 1
                  }
                }
              }

              val body = content.substring(bodyStart, i)
              i += 1

              val (properties, nestedContent) = parseBodyContent(body)

              val fullSelector = if (parentSelector.isEmpty) {
                selector
              } else if (selector.contains("&")) {
                selector.replace("&", parentSelector)
              } else {
                s"$parentSelector $selector"
              }

              val children = parseBlock(nestedContent, fullSelector)

              rules += Rule(fullSelector, properties, children)
              rules ++= parseBlock(content.substring(i), parentSelector)
              rules.toList
            }
          }
        } else {
          i += 1
        }
      }
      return rules.toList
    }
    rules.toList
  }

  private def parseBodyContent(body: String): (List[String], String) = {
    val properties = mutable.ListBuffer[String]()
    val nestedContent = new StringBuilder
    var i = 0
    val len = body.length

    while (i < len) {
      while (i < len && body.charAt(i).isWhitespace) i += 1
      if (i >= len) return (properties.toList, nestedContent.toString)

      val lineStart = i

      var j = i
      var foundBrace = false
      var foundSemicolon = false
      while (j < len && !foundBrace && !foundSemicolon) {
        val c = body.charAt(j)
        if (c == '"' || c == '\'') {
          j = skipString(body, j, c)
        } else {
          c match {
            case '{' => foundBrace = true
            case ';' => foundSemicolon = true
            case _   => j += 1
          }
        }
      }

      if (foundBrace) {
        j += 1
        var depth = 1
        while (j < len && depth > 0) {
          val c = body.charAt(j)
          if (c == '"' || c == '\'') {
            j = skipString(body, j, c)
          } else {
            c match {
              case '{' => depth += 1; j += 1
              case '}' => depth -= 1; j += 1
              case _   => j += 1
            }
          }
        }
        nestedContent.append(body.substring(lineStart, j))
        nestedContent.append("\n")
        i = j
      } else if (foundSemicolon) {
        val prop = body.substring(lineStart, j).trim
        if (prop.nonEmpty) properties += prop
        i = j + 1
      } else {
        val prop = body.substring(lineStart, len).trim
        if (prop.nonEmpty && !prop.contains("{")) properties += prop
        i = len
      }
    }
    (properties.toList, nestedContent.toString)
  }

  private def renderRules(rules: List[Rule], sb: StringBuilder): Unit = {
    rules.foreach { rule =>
      if (rule.properties.nonEmpty) {
        sb.append(rule.selector)
        sb.append(" {\n")
        rule.properties.foreach { prop =>
          sb.append("  ")
          sb.append(prop)
          sb.append(";\n")
        }
        sb.append("}\n")
      }
      renderRules(rule.children, sb)
    }
  }
}

object CssMacro {
  private val classPattern = """\.([\w-]+)""".r

  given cssResultToString[T]: Conversion[(css: String, classNames: T), String] =
    result => result.css

  extension (inline sc: StringContext) {
    transparent inline def css(inline args: Any*): Any = ${
      cssInterpolatorImpl('sc, 'args)
    }
  }

  private def cssInterpolatorImpl(
      scExpr: Expr[StringContext],
      argsExpr: Expr[Seq[Any]]
  )(using Quotes): Expr[Any] = {
    import quotes.reflect.*

    val parts: List[String] = scExpr match {
      case '{ StringContext(${ Varargs(Exprs(parts)) }*) } => parts.toList
      case _ =>
        report.errorAndAbort("css interpolator requires literal string parts")
    }

    val allStaticCss = parts.mkString

    validateCss(allStaticCss) match {
      case Some(error) => report.errorAndAbort(s"CSS syntax error: $error")
      case None        =>
    }

    val flattenedStaticCss = flattenNestedCss(allStaticCss)
    val classNames = extractClassNames(flattenedStaticCss)

    val fields = classNames.map(name => (name, name)).distinctBy(_._1)

    val (classNamesTupleExpr, classNamesType) = buildClassNamesTuple(fields)

    val cssStringExpr = argsExpr match {
      case '{ Seq() } | '{ Nil } | '{ Seq.empty } =>
        Expr(flattenedStaticCss)
      case Varargs(argExprs) if argExprs.isEmpty =>
        Expr(flattenedStaticCss)
      case _ =>
        '{
          val partsIter = $scExpr.parts.iterator
          val argsIter = $argsExpr.iterator
          val sb = new StringBuilder(${ Expr(allStaticCss.length + 64) })
          while (partsIter.hasNext) {
            sb.append(partsIter.next())
            if (argsIter.hasNext) sb.append(argsIter.next().toString)
          }
          CssFlattener.flatten(sb.toString)
        }
    }

    buildResultTuple(cssStringExpr, classNamesTupleExpr, classNamesType)
  }

  private def extractClassNames(css: String): List[String] =
    classPattern.findAllMatchIn(css).map(_.group(1)).toList.distinct

  private def validateCss(css: String): Option[String] = {
    val trimmed = css.trim
    if (trimmed.isEmpty) None
    else checkBalancedBraces(css).orElse(checkMissingSelector(trimmed))
  }

  private def checkBalancedBraces(css: String): Option[String] = {
    var braceCount = 0
    var inString = false
    var stringChar = ' '
    var i = 0
    while (i < css.length) {
      val c = css(i)
      if (inString) {
        if (c == stringChar && (i == 0 || css(i - 1) != '\\')) {
          inString = false
        }
      } else {
        c match {
          case '"' | '\'' =>
            inString = true
            stringChar = c
          case '{' => braceCount += 1
          case '}' =>
            braceCount -= 1
            if (braceCount < 0) {
              return Some(s"Unexpected '}' at position $i - no matching '{'")
            }
          case _ =>
        }
      }
      i += 1
    }
    if (braceCount != 0) {
      Some(
        s"Unbalanced braces: ${Math.abs(braceCount)} unclosed '${"{"}'${
            if (braceCount > 1) "s"
            else ""
          }"
      )
    } else None
  }

  private def checkMissingSelector(css: String): Option[String] = {
    val selectorBeforeBrace = """^\s*\{""".r
    if (selectorBeforeBrace.findFirstIn(css).isDefined) {
      Some("Missing selector before '{'")
    } else None
  }

  private def flattenNestedCss(css: String): String =
    CssFlattener.flatten(css)

  private def buildClassNamesTuple(
      fields: List[(String, String)]
  )(using q: Quotes): (Expr[Any], q.reflect.TypeRepr) = {
    import q.reflect.*

    fields match {
      case Nil =>
        (
          '{ EmptyTuple },
          TypeRepr.of[NamedTuple.NamedTuple[EmptyTuple, EmptyTuple]]
        )
      case _ =>
        val tupleExpr = Expr.ofTupleFromSeq(fields.map(f => Expr(f._2)))

        val labelsTupleType = fields.foldRight(TypeRepr.of[EmptyTuple]) {
          case ((fieldName, _), acc) =>
            TypeRepr
              .of[*:]
              .appliedTo(List(ConstantType(StringConstant(fieldName)), acc))
        }

        val valuesTupleType = fields.foldRight(TypeRepr.of[EmptyTuple]) {
          (_, acc) =>
            TypeRepr.of[*:].appliedTo(List(TypeRepr.of[String], acc))
        }

        val namedTupleType = TypeRepr
          .of[NamedTuple.NamedTuple]
          .appliedTo(List(labelsTupleType, valuesTupleType))

        val expr = namedTupleType.asType match {
          case '[t] => '{ $tupleExpr.asInstanceOf[t] }
        }

        (expr, namedTupleType)
    }
  }

  private def buildResultTuple(
      cssExpr: Expr[String],
      classNamesExpr: Expr[Any],
      classNamesType: Any
  )(using q: Quotes): Expr[Any] = {
    import q.reflect.*
    val classNamesTypeRepr = classNamesType.asInstanceOf[TypeRepr]

    val outerLabelsTupleType = TypeRepr
      .of[*:]
      .appliedTo(
        List(
          ConstantType(StringConstant("css")),
          TypeRepr
            .of[*:]
            .appliedTo(
              List(
                ConstantType(StringConstant("classNames")),
                TypeRepr.of[EmptyTuple]
              )
            )
        )
      )

    val outerValuesTupleType = TypeRepr
      .of[*:]
      .appliedTo(
        List(
          TypeRepr.of[String],
          TypeRepr
            .of[*:]
            .appliedTo(
              List(
                classNamesTypeRepr,
                TypeRepr.of[EmptyTuple]
              )
            )
        )
      )

    val outerNamedTupleType = TypeRepr
      .of[NamedTuple.NamedTuple]
      .appliedTo(List(outerLabelsTupleType, outerValuesTupleType))

    outerNamedTupleType.asType match {
      case '[t] =>
        '{ ($cssExpr, $classNamesExpr).asInstanceOf[t] }
    }
  }
}
