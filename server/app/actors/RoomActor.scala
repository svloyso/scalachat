package actors

import akka.actor._
import dao.MessagesDAO
import models.Message
import org.joda.time.DateTime
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable

/**
  * Created by svloyso on 26.12.16.
  */
class RoomActor (room: String, messages: MessagesDAO)  extends Actor {
  var listeners: mutable.HashMap[String, ActorRef] = new mutable.HashMap()
  def receive = {
    case ConnectMessage(name) =>
      Logger.info(s"Got an connect message from $name")
      val s = sender()
      val now = DateTime.now.getMillis
      messages.lastTen(room).foreach(_.foreach{m =>
        s ! OutcomingMessage(m.user, m.text, m.dateTime)
      })
      listeners.values.foreach { out =>
        out ! ConnectedMessage(name, now)
      }
      listeners(name) = s
      s ! ListOfUserMessage(listeners.keys.toSeq)
    case IncomingMessage(user, msg) =>
      Logger.info(s"[$room][$user] $msg")
      val now = DateTime.now.getMillis
      listeners.values.foreach {out =>
        out ! OutcomingMessage(user, msg, now)
      }
      messages.insert(Message(Some(0), room, user, msg, now))
    case DisconnectMessage(name) =>
      Logger.info(s"Got an disconnect message from $name")
      listeners.remove(name)
      listeners.values.foreach {out =>
        out ! DisconnectedMessage(name, DateTime.now.getMillis)
      }
  }
}

object RoomActor {
  def props(room: String, messages: MessagesDAO) = Props(new RoomActor(room, messages))
}


sealed trait InMessage
sealed trait OutMessage
case class IncomingMessage(user: String, msg: String) extends InMessage
case class OutcomingMessage(user: String, msg: String, dt: Long) extends OutMessage
case class ConnectMessage(name: String) extends InMessage
case class ConnectedMessage(name: String, dt: Long) extends OutMessage
case class DisconnectMessage(name: String) extends InMessage
case class DisconnectedMessage(name: String, dt: Long) extends OutMessage
case class ListOfUserMessage(users: Seq[String]) extends OutMessage