package io.github.locl95.smashtools.smashgg.domain

case class Tournament(id:Int, name: String)
case class Participant(ids: List[Int]) //Players
case class Event(id:Int, name: String, idTournament: Int = 0)
case class Entrant(id:Int, idEvent:Int, name: String)
case class PlayerStanding(placement: Int, idEntrant: Int)
case class Phase(id: Int, name:String)
case class Sets(id:Int, idEvent:Int, scores: List[Score])
case class Score(idPlayer: Int, score: Int)
case class AuthToken(id: Long)