package fiddle

import org.scalajs.dom

/**
 * Things that should eventually be pushed upstream to one of the libraries
 * that scala-js-fiddle depends on.
 */
object Util {

  /**
   * Creates a HTML node from the given string
   */
  def createDom(s: String) = {
    val parser = new dom.DOMParser
    dom.document.adoptNode(
      parser.parseFromString(s, "text/html").documentElement
    ).lastChild.lastChild
  }
  def getElem[T](id: String) = dom.document.getElementById(id).asInstanceOf[T]

  object Form{
    def post(path: String, args: (String, String)*): Unit = {
      ajax("post", path, args:_*)
    }
    def get(path: String, args: (String, String)*): Unit = {
      ajax("get", path, args:_*)
    }
    def ajax(method: String, path: String, args: (String, String)*): Unit = {
      val form = dom.document.createElement("form").asInstanceOf[dom.HTMLFormElement]
      form.setAttribute("method", method)
      form.setAttribute("action", path)

      for((k, v) <- args){
        val hiddenField = dom.document.createElement("input")
        hiddenField.setAttribute("type", "hidden")
        hiddenField.setAttribute("name", k)
        hiddenField.setAttribute("value", v)
        form.appendChild(hiddenField)
      }

      dom.document.body.appendChild(form)
      form.submit()
    }
  }
}