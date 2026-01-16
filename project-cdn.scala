//> using scala 3.8.0
//> using platform js
//> using jsModuleKind nomodule
//> using dep com.raquo::laminar::17.2.1
//> using dep org.scala-js::scalajs-dom::2.8.1
//> using file src/LaminarWebComponent.scala
//> using file src/ReactiveAttr.scala
//> using file src/ReactiveProp.scala
//> using file src/css.scala
//> using file src/cdn.scala
//> using file src/components/TuButton.scala
//> using file src/components/TuIcon.scala
//> using file src/components/TuInput.scala
//> using file src/components/TuCheckbox.scala
//> using file src/components/TuRadio.scala
//> using file src/components/TuTextarea.scala
//> using file src/components/TuSelect.scala

// CDN build - outputs a single JS file that auto-registers all components
// Build with: scala-cli --power package project-cdn.scala -f -o tu-components.js
// Test with: open cdn-demo.html
