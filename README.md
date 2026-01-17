# Laminar Web Components

A framework for building type-safe Web Components using [Laminar](https://laminar.dev) and Scala 3.

## Type-Safe String Union Attributes

This framework provides a macro-based solution for defining string union attributes with **both syntax sugar AND compile-time type safety**.

### The Problem

Web Components often have attributes that accept a fixed set of string values (e.g., `variant="primary"`, `size="small"`). In TypeScript, you'd define these as union types like `"primary" | "secondary" | "outline"`.

A naive Scala implementation using `Dynamic.selectDynamic` allows nice syntax like `_.variant.primary`, but provides **no type safety** - typos like `_.variant.primattry` compile without error.

### The Solution

We use Scala 3's `Selectable` trait combined with a **transparent inline macro** that generates refinement types at compile time.

#### How It Works

1. **Define a union type** for valid attribute values:

```scala
type Variant = "primary" | "secondary" | "outline" | "ghost"
```

2. **Create the attribute** using `attr.stringUnion`:

```scala
val variant = attr.stringUnion[Variant]("variant", "primary")
```

3. **The macro generates a refinement type** like:

```scala
StringUnionAttr[Variant] {
  val primary: Setter[HtmlElement]
  val secondary: Setter[HtmlElement]
  val outline: Setter[HtmlElement]
  val ghost: Setter[HtmlElement]
}
```

4. **Use with nice syntax AND type safety**:

```scala
TuButton(_.variant.primary)("Click me")   // ✓ Compiles
TuButton(_.variant.secondary)("Click me") // ✓ Compiles
TuButton(_.variant.primattry)("Click me") // ✗ Compile error!
```

The compile error clearly shows valid options:
```
value primattry is not a member of StringUnionAttr[TuButton.Variant]{
  val primary: Setter[...]
  val secondary: Setter[...]
  val outline: Setter[...]
  val ghost: Setter[...]
}
```

### Implementation Details

The magic happens in `StringUnionAttr.apply`:

```scala
transparent inline def apply[T <: String](
    attrName: String,
    default: T,
    reflect: Boolean = false
)(using uv: UnionValues[T]): StringUnionAttr[T] = ${ applyImpl[T](...) }
```

The macro:
1. Extracts literal string values from the union type `T` at compile time
2. Builds a refinement type with a `val` for each literal value
3. Returns the `StringUnionAttr` cast to this refined type

At runtime, `Selectable.selectDynamic` is called, which simply creates the attribute setter:

```scala
def selectDynamic(name: String): Setter[HtmlElement] = this := name.asInstanceOf[T]
```

### Example Component

```scala
object TuButton extends LaminarWebComponent("tu-button") {
  type Variant = "primary" | "secondary" | "outline" | "ghost"
  type Size = "small" | "medium" | "large"

  val variant = attr.stringUnion[Variant]("variant", "primary")
  val btnSize = attr.stringUnion[Size]("size", "medium")
  val btnDisabled = attr.boolean("disabled", false)

  override def render: View = button(
    cls <-- variant.signal.combineWith(btnSize.signal).map((v, s) => s"btn $v $s"),
    disabled <-- btnDisabled.signal,
    slotElement()
  )
}
```

## Build

```bash
scala-cli --power package project.scala src -f -o main.js
```

## References

- [MDN: Using Custom Elements](https://developer.mozilla.org/en-US/docs/Web/API/Web_components/Using_custom_elements)
- [Laminar](https://laminar.dev)
- [Scala 3 Selectable](https://docs.scala-lang.org/scala3/reference/changed-features/structural-types.html)