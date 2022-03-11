package io.github.locl95.smashtools
//
//import cats.data.{Kleisli, OptionT}
//import cats.effect.Sync
//import io.github.locl95.smashtools.smashgg.domain.AuthToken
//import org.http4s.Status.Forbidden
//import org.http4s.headers.Authorization
//import org.http4s.server.AuthMiddleware
//import org.http4s.{AuthedRoutes, HttpRoutes, Request}
//
//final class SmashggAuth[F[_]: Sync] {
//
//  //crypto.validateSignedToken(h.value).map(v => AuthToken(v.toLong)).toRight("Invalid token").pure[F]
//  val userCredentials: List[(String, Int)] = List(("XAVI", 321))
//  val permissionRoles: List[(String, String)] = List(("TOURNAMENT-ORGANIZER", "IMPORT-TOURNAMENT"), ("TOURNAMENT-ORGANIZER", "EDIT-TOURNAMENT"))
//  val credentialRoles = List((321, "TOURNAMENT-ORGANIZER"))
//
//  def findTokenApiFromUserToken(headerValue: String): F[Option[AuthToken]] = ???
//
//  val authToken: Kleisli[F, Request[F], Either[String, AuthToken]] = Kleisli({
//    request => {
//        val header = request.headers.get(Authorization)
//        header match {
//          case Some(h) => ???
//          case None => ???
//        }
//    }
//  })
//
//  val onFailure: AuthedRoutes[String, F] = Kleisli(req => OptionT.liftF(Forbidden))
//
//  val middleware: AuthMiddleware[F, AuthToken] = AuthMiddleware(authToken, onFailure)
//
//  def apply(service: AuthedRoutes[AuthToken, F]): HttpRoutes[F] =
//    middleware(service)
//}
