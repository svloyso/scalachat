package actors
import akka.actor._
import upickle.default._

import shared.SocketMessage

/**
  * Created by svloyso on 26.12.16.
  */
class UserActor(name: String, out: ActorRef, room: ActorRef) extends Actor {
  room ! ConnectMessage(name)
  def receive = {
    case raw: String =>
      val msg = read[SocketMessage](raw)
      room ! RoomMessage(msg.user, msg.text)
    case RoomMessage(user, msg) =>
      out ! write(SocketMessage(user, msg))
    case ConnectedMessage(user) =>
      out ! write(SocketMessage("", user + " has joined to room"))
    case DisconnectedMessage(user) =>
      out ! write(SocketMessage("", user + " has leaved room"))
  }

  override def postStop = {
    room ! DisconnectMessage(name)
  }
}

object UserActor {
  def props(name: String, out: ActorRef, room: ActorRef) = Props(new UserActor(name, out, room))
}
