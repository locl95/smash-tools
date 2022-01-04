package io.github.locl95.smashtools.smashgg.domain

case class Tournament(name: String)
case class Participant(ids: List[Int])
case class Event(name: String)
case class Entrant(name: String)
case class PlayerStanding(placement: Int, idEntrant: Int)
case class Phase(id: Int, name:String)
