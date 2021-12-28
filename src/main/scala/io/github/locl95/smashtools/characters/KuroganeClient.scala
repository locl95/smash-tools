package io.github.locl95.smashtools.characters
import cats.effect.Sync
import org.http4s.{EntityDecoder, Uri}
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

//TODO: Implement KuroganeClient. Should be able to retreat Characters, CharactersMoves and Moves

trait KuroganeClient[F[_]] {
  def get[A](uri:Uri)(implicit decoder: EntityDecoder[F, A]): F[A]
}

case class KuroganeClientError(t: Throwable) extends RuntimeException


object KuroganeClient {
  def impl[F[_]: Sync](C: Client[F]): KuroganeClient[F] = new KuroganeClient[F]{
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F]{}
    import dsl._
    def get[A](uri: Uri)(implicit decoder: EntityDecoder[F, A]): F[A] = {
        C.expect[A](GET(uri))
    }
  }
}
