package gclaramunt.unichain

import scala.util.{Failure, Success, Try}

trait UnichainError:
  val msg:String

case class GenericUnichainError(msg: String) extends UnichainError
case class ExceptionUnichainError(e: Throwable) extends UnichainError:
  val msg: String = e.getMessage

object UnichainError:
  def fromTry[T](t: Try[T]): Either[UnichainError, T] =
    t.fold(e => Left(ExceptionUnichainError(e)), t => Right(t))

  def toTry[T](ue: Either[UnichainError, T]): Try[T] = ue.fold(err => Failure(new Exception(err.msg)), v => Success(v))