package scalafiddle.shared

case class ExtLib(group: String, artifact: String, version: String, compileTimeOnly: Boolean) {
  override def toString: String = s"$group ${if (compileTimeOnly) "%%" else "%%%"} $artifact % $version"
}

object ExtLib {
  implicit val rw: upickle.default.ReadWriter[ExtLib] = upickle.default.macroRW

  val repoSJSRE = """([^ %]+) *%%% *([^ %]+) *% *([^ %]+)""".r
  val repoRE    = """([^ %]+) *%% *([^ %]+) *% *([^ %]+)""".r

  def apply(libDef: String): ExtLib = libDef match {
    case repoSJSRE(group, artifact, version) =>
      ExtLib(group, artifact, version, false)
    case repoRE(group, artifact, version) =>
      ExtLib(group, artifact, version, true)
    case _ =>
      throw new IllegalArgumentException(s"Library definition '$libDef' is not correct")
  }
}
