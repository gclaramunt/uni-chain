package gclaramunt

package object unichain {
  trait UnichainError:
    val msg:String

  case class GenericUnichainError(msg: String) extends UnichainError
}
