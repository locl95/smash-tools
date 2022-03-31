package io.github.locl95.smashtools.smashgg

import cats.effect.concurrent.Ref
import cats.effect.{IO, Sync}
import cats.implicits._
import io.github.locl95.smashtools.JdbcDatabaseConfiguration
import io.github.locl95.smashtools.smashgg.domain._
import io.github.locl95.smashtools.smashgg.repository._
import org.http4s._

final class TournamentsInMemoryRepository[F[_]: Sync](ref: Ref[F, List[Tournament]]) extends TournamentRepository[F] {
  override def toString: String = "TournamentsInMemoryRepository"

  override def insert(tournament: Tournament): F[Int] =
    for {
      _ <- ref.update(_ ++ List(tournament))
      tournaments <- ref.get
    } yield tournaments.find(_.id == tournament.id).get.id

  override def get: F[List[Tournament]] =
    ref.get

  override def get(id: Int): F[Option[Tournament]] =
    ref.get.map(_.find(_.id == id))
}

object TournamentsInMemoryRepository {
  def apply[F[_]: Sync]: F[TournamentsInMemoryRepository[F]] =
    Ref.of[F, List[Tournament]](List.empty).map(new TournamentsInMemoryRepository[F](_))
}

final class EntrantInMemoryRepository[F[_]: Sync](ref: Ref[F, List[Entrant]]) extends EntrantRepository[F] {

  override def toString:String =
    "EntrantInMemoryRepository"

  override def insert(entrants: List[Entrant]): F[Int] = {
    for {
      _ <- ref.update(_ ++ entrants)
      _entrants <- ref.get
    } yield _entrants.size
  }
  override def getEntrants(eventID: Int): F[List[Entrant]] =
    ref.get.map(_.filter(x => x.idEvent == eventID))

  override def getEntrants: F[List[Entrant]] =
    ref.get

}

object EntrantInMemoryRepository {
  def apply[F[_]: Sync]: F[EntrantInMemoryRepository[F]] =
    Ref.of[F, List[Entrant]](List.empty).map(new EntrantInMemoryRepository[F](_))
}


final class EventInMemoryRepository[F[_]: Sync](ref: Ref[F, List[Event]]) extends EventRepository[F] {

  override def toString:String =
    "EventInMemoryRepository"

  override def insert(event: Event): F[Int] =
    for {
      _ <- ref.update(_ ++ List(event))
      events <- ref.get
    } yield events.find(_.id == event.id).get.id


  override def get: F[List[Event]] =
    ref.get

  override def getEvents(tournament: Int): F[List[Event]] =
    ref.get.map(_.filter(_.idTournament == tournament))
}

object EventInMemoryRepository {
  def apply[F[_]: Sync]: F[EventInMemoryRepository[F]] =
    Ref.of[F, List[Event]](List.empty).map(new EventInMemoryRepository[F](_))
}

final class PhaseInMemoryRepository[F[_]: Sync](ref: Ref[F, List[Phase]]) extends PhaseRepository[F]{

  override def toString:String =
    "PhaseInMemoryRepository"

  override def insert(phases: List[Phase]): F[Int] =
    for {
      _ <- ref.update(_ ++ phases)
      _phases <- ref.get
    } yield _phases.size

  override def getPhases: F[List[Phase]] =
    ref.get
}

object PhaseInMemoryRepository {
  def apply[F[_]: Sync]: F[PhaseInMemoryRepository[F]] =
    Ref.of[F, List[Phase]](List.empty).map(new PhaseInMemoryRepository[F](_))
}

final class SetsInMemoryRepository[F[_]: Sync](ref: Ref[F, List[Sets]]) extends SetsRepository[F]{
  override def toString:String =
    "SetsInMemoryRepository"

  override def insert(sets: List[Sets]): F[Int] =
    for {
      _ <- ref.update(_ ++ sets)
      _sets <- ref.get
    } yield _sets.size

  override def getSets: F[List[Sets]] =
    ref.get
}

object SetsInMemoryRepository {
  def apply[F[_]: Sync]: F[SetsInMemoryRepository[F]] =
    Ref.of[F, List[Sets]](List.empty).map(new SetsInMemoryRepository[F](_))
}

object TestHelper {
  val databaseTestConfig: JdbcDatabaseConfiguration = JdbcDatabaseConfiguration("org.postgresql.Driver", "jdbc:postgresql:smashtools", "test", "test", 5, 10)

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

  def checkF[A](actualResp:        Response[IO],
               expectedStatus: Status,
               expectedBody:   Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): IO[Boolean] =  {

    val statusCheck        = actualResp.status == expectedStatus
    val bodyCheck          = expectedBody.fold[IO[Boolean]](
      // Verify Response's body is empty.
      actualResp.body.compile.toVector.map(_.isEmpty))(
      expected => actualResp.as[A].map(_ == expected)
    )

    bodyCheck.map(_ && statusCheck)
  }
}
