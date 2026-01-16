## Scala Preferences
- Using Scala 3, but prefer curly braces over indentation-based syntax

## Build

```bash
scala-cli --power package project.scala src -f -o main.js
```

## Workflow
- After changing code, always run the build command to verify compilation succeeds

## File Structure
- `project.scala` - Centralized build configuration (using directives)
- `src/`
  - `macros.scala` - ReactiveAttr class and compile-time macro for extracting attributes
  - `ReactiveProp.scala` - Instance-level reactive property wrapper (Laminar Var/Signal)
  - `HelloWorld.scala` - Example web component implementation
  - `main.scala` - Application entry point
