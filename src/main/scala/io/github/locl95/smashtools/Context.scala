package io.github.locl95.smashtools

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import io.github.locl95.smashtools.characters.repository.{CharacterPostgresRepository, MovementPostgresRepository}
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import io.github.locl95.smashtools.characters.{CharactersRoutes, KuroganeClient}
import io.github.locl95.smashtools.smashgg.repository._
import io.github.locl95.smashtools.smashgg.service._
import io.github.locl95.smashtools.smashgg.{SmashggClient, SmashggRoutes}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

final case class Context[F[_]: ConcurrentEffect](transactor: HikariTransactor[F]) {

  val charactersRoutesProgram: Resource[F, CharactersRoutes[F]] = for {
    client <- BlazeClientBuilder[F](global).resource
    kuroganeClient = KuroganeClient.impl[F](client)
  } yield new CharactersRoutes[F](
    new CharactersService[F](new CharacterPostgresRepository[F](transactor), kuroganeClient),
    new MovementsService[F](new MovementPostgresRepository[F](transactor), kuroganeClient)
  )

  val smashggRoutesProgram: Resource[F, SmashggRoutes[F]] =
    for {
      client <- BlazeClientBuilder[F](global).resource
      smashggClient = SmashggClient.impl[F](client)
    } yield new SmashggRoutes[F](
      new TournamentService[F](new TournamentPostgresRepository[F](transactor), smashggClient),
      new EventService[F](new EventPostgresRepository[F](transactor), smashggClient),
      new EntrantService[F](new EntrantPostgresRepository[F](transactor), smashggClient),
      new PhaseService[F](new PhasePostgresRepository[F](transactor), smashggClient),
      new SetsService[F](new SetsPostgresRepository[F](transactor), smashggClient)
    )


}

object Context {
  def production[F[_]: ConcurrentEffect: ContextShift](config: JdbcDatabaseConfiguration): Resource[F, Context[F]] = for {
    blocker <- Blocker.apply
    transactor <- {
      implicit val b: Blocker = blocker
      JdbcTransactor.transactorResource(config, _ => Async[F].pure(()))
    }
  } yield new Context[F](transactor)

  def test[F[_]: ConcurrentEffect: ContextShift](config: JdbcDatabaseConfiguration): Resource[F, Context[F]] = for {
    blocker <- Blocker.apply
    transactor <- {
      implicit val b: Blocker = blocker
      JdbcTestTransactor.transactorResource(config)
    }
  } yield new Context[F](transactor)

}
