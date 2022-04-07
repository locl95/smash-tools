package io.github.locl95.smashtools

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, Resource}
import io.github.locl95.smashtools.characters.repository.{CharacterPostgresRepository, CharactersRepository, MovementPostgresRepository, MovementsRepository}
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import io.github.locl95.smashtools.characters.{CharactersRoutes, KuroganeClient}
import io.github.locl95.smashtools.smashgg.repository._
import io.github.locl95.smashtools.smashgg.service._
import io.github.locl95.smashtools.smashgg.{SmashggClient, SmashggRoutes}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

final case class Context[F[_]: ConcurrentEffect](characterRepository: CharactersRepository[F],
                                                 movementRepository: MovementsRepository[F],
                                                 tournamentRepository: TournamentRepository[F],
                                                 eventRepository: EventRepository[F],
                                                 phaseRepository: PhaseRepository[F],
                                                 setsRepository: SetsRepository[F],
                                                 entrantRepository: EntrantRepository[F]
                                                ) {

  val charactersRoutesProgram: Resource[F, CharactersRoutes[F]] = for {
    client <- BlazeClientBuilder[F](global).resource
    kuroganeClient = KuroganeClient.impl[F](client)
  } yield new CharactersRoutes[F](
    new CharactersService[F](characterRepository, kuroganeClient),
    new MovementsService[F](movementRepository, kuroganeClient)
  )

  val smashggRoutesProgram: Resource[F, SmashggRoutes[F]] =
    for {
      client <- BlazeClientBuilder[F](global).resource
      smashggClient = SmashggClient.impl[F](client)
    } yield new SmashggRoutes[F](
      new TournamentService[F](tournamentRepository, smashggClient),
      new EventService[F](eventRepository, smashggClient),
      new EntrantService[F](entrantRepository, smashggClient),
      new PhaseService[F](phaseRepository, smashggClient),
      new SetsService[F](setsRepository, smashggClient)
    )
}

object Context {
  def production[F[_]: ConcurrentEffect: ContextShift](config: JdbcDatabaseConfiguration): Resource[F, Context[F]] = for {
    blocker <- Blocker.apply
    transactor <- {
      implicit val b: Blocker = blocker
      JdbcTransactor.transactorResource(config, _ => Async[F].pure(()))
    }
    charactersPostgresRepo = new CharacterPostgresRepository[F](transactor)
    movementsPostgresRepo = new MovementPostgresRepository[F](transactor)

    tournamentPostgresRepo = new TournamentPostgresRepository[F](transactor)
    eventPostgresRepo = new EventPostgresRepository[F](transactor)
    entrantPostgresRepo = new EntrantPostgresRepository[F](transactor)
    phasePostgresRepo = new PhasePostgresRepository[F](transactor)
    setsPostgresRepo = new SetsPostgresRepository[F](transactor)
  } yield new Context[F](charactersPostgresRepo, movementsPostgresRepo,
    tournamentPostgresRepo, eventPostgresRepo, phasePostgresRepo, setsPostgresRepo, entrantPostgresRepo)
}
