package kojo

object Speed {
  trait Speed
  case object slow extends Speed
  case object medium extends Speed
  case object fast extends Speed
  case object superFast extends Speed
}
