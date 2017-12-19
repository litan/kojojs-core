// Borrowed from: https://github.com/underscoreio/doodle
package kojo.syntax

import kojo.doodle.UnsignedByte

trait UnsignedByteSyntax {
  implicit class ToUnsignedByteOps(val value: Int) {
    def uByte: UnsignedByte =
      UnsignedByte.clip(value)
  }
}
