package scalafiddle.compiler.cache

import org.scalajs.linker.interface.Linker

object LinkerCache extends LRUCache[Linker]("Linker") {}
