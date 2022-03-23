package io.github.locl95.smashtools

import cats.data.Kleisli
import cats.effect.{Async, ConcurrentEffect, ContextShift, Resource, Timer}
import fs2.Stream
import cats.implicits.toSemigroupKOps
import io.github.locl95.smashtools.smashgg.{CredentialsInMemoryRepository, User, UsersInMemoryRepository}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.AuthMiddleware
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{HttpApp, Request, Response}

import scala.concurrent.ExecutionContext.global

object SmashtoolsServer {

  def stream[F[_]: ConcurrentEffect: ContextShift](implicit T: Timer[F]): Stream[F, Nothing] = {

    val context: Resource[F, Context[F]] =
      Context.production[F](JdbcDatabaseConfiguration("org.postgresql.Driver", "jdbc:postgresql:smashtools", "test", "test", 5, 10))

    val authMiddleware: AuthMiddleware[F, User] = SmashggAuth.make[F](new UsersInMemoryRepository[F],new CredentialsInMemoryRepository[F]).middleware

    for {

      ctx <- Stream.resource(context)
      charactersRoutes <- Stream.resource(ctx.charactersRoutesProgram)
      smashggRoutes <- Stream.resource(ctx.smashggRoutesProgram)

      httpApp: Kleisli[F, Request[F], Response[F]] =
        (charactersRoutes.characterRoutes <+> smashggRoutes.smashggRoutes <+> authMiddleware(smashggRoutes.authedSmashhggRoutes)).orNotFound

      finalHttpApp: HttpApp[F] = Logger.httpApp(true, true)(httpApp)

      port <- Stream.eval(Async[F].delay(Option(System.getenv("PORT")).getOrElse("8080").toInt))

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
