package io.github.locl95.smashtools.smashgg

import cats.effect.{IO, Sync}
import cats.implicits._
import io.github.locl95.smashtools.smashgg.domain._
import io.github.locl95.smashtools.smashgg.repository._
import org.http4s.{EntityDecoder, Header, Headers, Response, Status}

import scala.collection.mutable

trait InMemoryRepository {
  def clean(): Unit
}

final class TournamentsInMemoryRepository[F[_]: Sync] extends TournamentRepository[F] with InMemoryRepository{
  private val tournamentsList: mutable.ArrayDeque[Tournament] = mutable.ArrayDeque.empty
  override def toString: String = "TournamentsInMemoryRepository"
  override def insert(tournament: Tournament): F[Int] = {
    tournamentsList.append(tournament).pure[F]
    tournament.id.pure[F]
  }
  override def clean(): Unit = {
    tournamentsList.clearAndShrink()
  }
  override def get: F[List[Tournament]] =
    tournamentsList.toList.pure[F]

  override def get(id: Int): F[Option[Tournament]] =
    tournamentsList.find(_.id == id).pure[F]
}

final class EntrantInMemoryRepository[F[_]: Sync] extends EntrantRepository[F] with InMemoryRepository {
  private val entrantsArray: mutable.ArrayDeque[Entrant] = mutable.ArrayDeque.empty
  override def toString:String =
    "EntrantInMemoryRepository"
  override def insert(entrants: List[Entrant]): F[Int] = {
    entrantsArray.appendAll(entrants).pure[F]
    entrantsArray.size.pure[F]
  }

  override def getEntrants(eventID: Int): F[List[Entrant]] =
    entrantsArray.filter(x => x.idEvent == eventID).toList.pure[F]

  override def getEntrants: F[List[Entrant]] =
    entrantsArray.toList.pure[F]

  override def clean(): Unit =
    entrantsArray.clearAndShrink()
}

final class EventInMemoryRepository[F[_]: Sync] extends EventRepository[F] with InMemoryRepository {
  private val eventsArray: mutable.ArrayDeque[Event] = mutable.ArrayDeque.empty
  override def toString:String =
    "EventInMemoryRepository"
  override def insert(event: Event): F[Int] = {
    eventsArray.append(event).pure[F]
    event.id.pure[F]
  }
  override def clean(): Unit =
    eventsArray.clearAndShrink()

  override def get: F[List[Event]] =
    eventsArray.toList.pure[F]

  override def getEvents(tournament: Int): F[List[Event]] =
    eventsArray.toList.filter(_.idTournament == tournament).pure[F]
}

final class PlayerStandingInMemoryRepository[F[_]: Sync] extends PlayerStandingRepository[F] with InMemoryRepository {
  private val playerStandingsArray: mutable.ArrayDeque[PlayerStanding] = mutable.ArrayDeque.empty
  override def toString:String =
    "PlayerStandingInMemoryRepository"
  override def insert(playerStanding: List[PlayerStanding]): F[Int] = {
    playerStandingsArray.appendAll(playerStanding).pure[F]
    playerStandingsArray.size.pure[F]
  }
  override def clean(): Unit =
    playerStandingsArray.clearAndShrink()

  override def getPlayerStandings: F[List[PlayerStanding]] =
    playerStandingsArray.toList.pure[F]
}

final class PhaseInMemoryRepository[F[_]: Sync] extends PhaseRepository[F] with InMemoryRepository {
  private val phashesArray: mutable.ArrayDeque[Phase] = mutable.ArrayDeque.empty
  override def toString:String =
    "PhaseInMemoryRepository"
  override def insert(phases: List[Phase]): F[Int] = {
    phashesArray.appendAll(phases).pure[F]
    phashesArray.size.pure[F]
  }
  override def clean(): Unit =
    phashesArray.clearAndShrink()

  override def getPhases: F[List[Phase]] =
    phashesArray.toList.pure[F]
}

final class SetsInMemoryRepository[F[_]: Sync] extends SetsRepository[F] with InMemoryRepository
{
  private val setsArray: mutable.ArrayDeque[Sets] = mutable.ArrayDeque.empty
  override def toString:String =
    "SetsInMemoryRepository"
  override def insert(sets: List[Sets]): F[Int] = {
    setsArray.appendAll(sets).pure[F]
    setsArray.size.pure[F]
  }
  override def clean(): Unit =
    setsArray.clearAndShrink()

  override def getSets: F[List[Sets]] =
    setsArray.toList.pure[F]
}

object TestHelper {
  val tournament: Tournament = Tournament(312932,"MST-4")
  val tournaments: List[Tournament] = List(Tournament(312932,"MST-4"))

  val participants:List[Participant] =
    List(
      Participant(List(8022537)),
      Participant(List(7914930)),
      Participant(List(7919929, 7914930))
    )
  val entrant: Entrant = Entrant(8348984, 615463, "Raiden's | Zandark")
  val entrants: List[Entrant] = List(Entrant(8348984, 615463, "Raiden's | Zandark"), Entrant(8346516, 615463, "FS | Sevro"))
  val event: Event = Event(615463, "Ultimate Singles")
  val testEvent: Event = Event(615463, "Ultimate Singles", 312932)
  val events: List[Event] = List(Event(615463, "Ultimate Singles", 312932))
  val playerStandings: List[PlayerStanding] = List(PlayerStanding(1,8232866), PlayerStanding(2,8280489))
  val phases: List[Phase] = List(Phase(991477, "Bracket Pools"), Phase(991478, "Top 16"))
  val sets: List[Sets] = List(Sets(40865697,615463,List(Score(8280489,0),Score(8232866,3))), Sets(40865698,615463,List(Score(8232866,3),Score(8280489,2))))
  val testSets: List[Sets] = List(Sets(40865697,615463, List(Score(8232866, 3), Score(8280489, 0))), Sets(40865698,615463, List(Score(8232866, 3), Score(8280489, 2))))

  val headers: Headers = Headers.apply(
    Header("Authorization", "Bearer 3305177ceda157c60fbc09b79e2ff987")
  )

  def check[A](actual:        IO[Response[IO]],
               expectedStatus: Status,
               expectedBody:   Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): Boolean =  {
    val actualResp         = actual.unsafeRunSync()
    val statusCheck        = actualResp.status == expectedStatus
    val bodyCheck          = expectedBody.fold[Boolean](
      // Verify Response's body is empty.
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty)(
      expected => actualResp.as[A].unsafeRunSync() == expected
    )
    statusCheck && bodyCheck
  }
}
