package io.github.locl95.smashtools.smashgg

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import io.github.locl95.smashtools.smashgg.domain.Tournament
import io.github.locl95.smashtools.smashgg.repository.TournamentRepository

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
}
