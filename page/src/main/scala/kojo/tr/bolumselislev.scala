package kojo.tr

trait BölümselİşlevYöntemleri extends TemelTürler {
  type Bölümselİşlev[D, R] = PartialFunction[D, R]

  implicit class BölümselİşlevMetotları[D, R](f: PartialFunction[D, R]) {
    def tanımlıMı(d: D) = f.isDefinedAt(d)
  }
}
