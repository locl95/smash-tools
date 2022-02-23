package io.github.locl95.smashtools.smashgg.service

import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.Event
import io.github.locl95.smashtools.smashgg.repository.EventRepository

final case class EventService[F[_]](eventRepository: EventRepository[F],
                                    client: SmashggClient[F]) {

    def insert(tournamedID:Int, event: Event): F[Int] =
      eventRepository.insert(Event(event.id, event.name, tournamedID))

    def get: F[List[Event]] =
      eventRepository.get

    def getEvents(tournament: Int): F[List[Event]] =
      eventRepository.getEvents(tournament)
}
