package io.github.locl95.smashtools.smashgg.domain

case class Tournament(name: String) //Tournaments(id)
case class Participant(ids: List[Int]) //Players
case class Event(name: String) //Events(id, torunament_id)
case class Entrant(name: String) // id,id_event
case class PlayerStanding(placement: Int, idEntrant: Int)
case class Phase(id: Int, name:String)
case class Sets(id:Int, id_event:Int, id_winner:Int = 0, id_loser:Int = 0)
//sets(id,id_event,id_winner,id_loser)
