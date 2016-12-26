package actors

import java.text.SimpleDateFormat

import akka.actor._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import upickle.default._

import shared._

/**
  * Created by svloyso on 26.12.16.
  */
class UserActor(name: String, out: ActorRef, room: ActorRef) extends Actor {
  val format = DateTimeFormat.forPattern("dd-MM-yyyy kk:mm:ss")
  def receive = {
    case raw: String =>
      val msg = read[ClientMessage](raw)
      room ! IncomingMessage(msg.user, msg.text)
    case OutcomingMessage(user, msg, dt) =>
      out ! write(ServerMessage(user, msg, format.print(new DateTime(dt))))
    case ConnectedMessage(user, dt) =>
      out ! write(JoinMessage(user, format.print(new DateTime(dt))))
    case DisconnectedMessage(user, dt) =>
      out ! write(LeaveMessage(user, format.print(new DateTime(dt))))
    case ListOfUserMessage(users) =>
      out ! write(UserlistMessage(users))
  }

  override def preStart = {
    room ! ConnectMessage(name)
  }

  override def postStop = {
    room ! DisconnectMessage(name)
  }
}

object UserActor {
  def props(name: String, out: ActorRef, room: ActorRef) = Props(new UserActor(name, out, room))
}
