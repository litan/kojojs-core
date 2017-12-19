// Borrowed from: https://github.com/underscoreio/doodle
package kojo.syntax

import kojo.doodle.Normalized

trait NormalizedSyntax {
  implicit class ToNormalizedOps(val value: Double) {
    def normalized: Normalized =
      Normalized.clip(value)
  }
}
