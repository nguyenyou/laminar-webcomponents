/** CDN entry point - registers all web components when the script loads.
  *
  * Usage in HTML:
  * {{{
  * <script src="https://cdn.example.com/tu-components.js"></script>
  * <tu-button variant="primary">Click me</tu-button>
  * }}}
  */
@main
def registerTuComponents(): Unit = {
  TuButton.register
  TuIcon.register
  TuInput.register
  TuCheckbox.register
  TuRadio.register
  TuTextarea.register
  TuSelect.register
  TuOption.register
}
