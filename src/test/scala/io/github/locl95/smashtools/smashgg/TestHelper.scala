package io.github.locl95.smashtools.smashgg

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import io.github.locl95.smashtools.smashgg.domain.{Participant, Tournament}
import io.github.locl95.smashtools.smashgg.repository.{ParticipantRepository, TournamentRepository}

import scala.collection.mutable

trait InMemoryRepository {
  def clean(): Unit
}

final class TournamentsInMemoryRepository[F[_]: Sync] extends TournamentRepository[F] with InMemoryRepository{
  private val tournamentsList: mutable.ArrayDeque[Tournament] = mutable.ArrayDeque.empty

  override def toString: String = "TournamentsInMemoryRepository"

  override def insert(tournaments: Tournament): F[Int] = {
    tournamentsList.append(tournaments).pure[F]
    1.pure[F]
  }

  /*
  override def getTournament(tournament: String): F[Option[Tournament]] =
    tournamentsList.find(_.name == tournament).pure[F]
*/
  override def clean(): Unit = {
    tournamentsList.clearAndShrink()
  }

  override def get: F[List[Tournament]] =
    tournamentsList.toList.pure[F]
}

final class ParticipantInMemoryRepository[F[_]: Sync] extends ParticipantRepository[F] with InMemoryRepository{
  private val participantsArray:mutable.ArrayDeque[Participant] = mutable.ArrayDeque.empty

  override def toString: String = "ParticipantInMemoryRepository"

  override def insert(participants: List[Participant]): F[Int] = ???

  override def get: F[List[Participant]] = ???

  override def clean(): Unit = ???
}

/*
final class SmashggClientMock[F[_]: Applicative] extends SmashggClient[F]{
  override def toString: String = "SmashggClientMock"

  override def getTournament(implicit  decocer: EntityDecoder[F, List[Tournament]]): F[List[Tournament]] =
    TestHelper.tournaments.pure[F]
}
*/

object TestHelper {
  val tournament: Tournament = Tournament("MST 4")
  val participants:List[Participant] =
    List(
      Participant(List(8022537, 7919929, 7914930))
    )
}
