package io.github.locl95.smashtools

import cats.Monad
import cats.data.{EitherT, Kleisli, OptionT}
import io.github.locl95.smashtools.smashgg.{User, Users}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRequest, AuthedRoutes, Request, Response}

final class SmashggAuth[F[_]: Monad](users: Users[F]) {
  private val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl._

  val authUser: Kleisli[F, Request[F], Either[String, User]] = Kleisli({
    request =>
      (for {
        header <- EitherT.fromOption[F](request.headers.get(Authorization), "Auth header is required")
        user <- EitherT(users.get(header.value.split(' ').last))
      } yield user).value
  })

  val onFailure: AuthedRoutes[String, F] = Kleisli[OptionT[F, *], AuthedRequest[F, String], Response[F]](req => OptionT.liftF(Forbidden(req.context)))

  val middleware: AuthMiddleware[F, User] = AuthMiddleware(authUser, onFailure)
}

object SmashggAuth {
  def make[F[_]: Monad](users: Users[F]): SmashggAuth[F] = new SmashggAuth[F](users)
}
