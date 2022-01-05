package io.github.locl95.smashtools.smashgg

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import io.github.locl95.smashtools.smashgg.domain.{Entrant, Event, Participant, Tournament}
import io.github.locl95.smashtools.smashgg.repository.{EntrantRepository, EventRepository, TournamentRepository}

import scala.collection.mutable

trait InMemoryRepository {
  def clean(): Unit
}

final class TournamentsInMemoryRepository[F[_]: Sync] extends TournamentRepository[F] with InMemoryRepository{
  private val tournamentsList: mutable.ArrayDeque[Tournament] = mutable.ArrayDeque.empty
  override def toString: String = "TournamentsInMemoryRepository"
  override def insert(tournament: Tournament): F[Int] = {
    tournamentsList.append(tournament).pure[F]
    1.pure[F]
  }
  override def clean(): Unit = {
    tournamentsList.clearAndShrink()
  }
  override def get: F[List[Tournament]] =
    tournamentsList.toList.pure[F]
}

final class EntrantInMemoryRepository[F[_]: Sync] extends EntrantRepository[F] with InMemoryRepository {
  private val entrantsArray: mutable.ArrayDeque[Entrant] = mutable.ArrayDeque.empty
  override def toString:String =
    "EntrantInMemoryRepository"
  override def insert(entrant: Entrant): F[Int] = {
    entrantsArray.append(entrant).pure[F]
    1.pure[F]
  }
  override def clean(): Unit =
    entrantsArray.clearAndShrink()
}

final class EventInMemoryRepository[F[_]: Sync] extends EventRepository[F] with InMemoryRepository {
  private val eventsArray: mutable.ArrayDeque[Event] = mutable.ArrayDeque.empty
  override def toString:String =
    "EventInMemoryRepository"
  override def insert(event: Event): F[Int] = {
    eventsArray.append(event).pure[F]
    1.pure[F]
  }
  override def clean(): Unit =
    eventsArray.clearAndShrink()
}

object TestHelper {
  val tournament: Tournament = Tournament("MST 4")
  val participants:List[Participant] =
    List(
      Participant(List(8022537)),
      Participant(List(7914930)),
      Participant(List(7919929, 7914930))
    )
  val entrant: Entrant = Entrant("Raiden's | Zandark")
  val event: Event = Event("Ultimate Singles")
}
