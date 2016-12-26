package actors

import akka.actor._
import dao.MessagesDAO
import models.Message
import org.joda.time.DateTime
import play.api.Logger
import shared.SocketMessage
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable

/**
  * Created by svloyso on 26.12.16.
  */
class RoomActor (room: String, messages: MessagesDAO)  extends Actor {
  var listeners: mutable.MutableList[ActorRef] = new mutable.MutableList()
  def receive = {
    case ConnectMessage(name) =>
      Logger.info(s"Got an connect message from $name")
      val s = sender()
      messages.lastTen(room).map(_.map{m =>
        Logger.info(s"History: [${m.user}] ${m.text}")
        s ! RoomMessage(m.user, m.text)
      })
      listeners.map {out =>
        out ! ConnectedMessage(name)
      }
      listeners += sender()
    case RoomMessage(user, msg) =>
      Logger.info(s"[$room][$user] $msg")
      if(msg == "list") {
        messages.all().map(s => Logger.info("Messages in database: " + s.size))
        messages.allInRoom(room).map(s => Logger.info("Messages in room: " + s.size))
      }
      listeners.map {out =>
        out ! RoomMessage(user, msg)
      }
      messages.insert(Message(Some(0), room, user, msg, DateTime.now.getMillis))
    case DisconnectMessage(name) =>
      Logger.info(s"Got an disconnect message from $name")
      listeners = listeners diff Seq(sender())
      listeners.map {out =>
        out ! DisconnectedMessage(name)
      }
  }
}

object RoomActor {
  def props(room: String, messages: MessagesDAO) = Props(new RoomActor(room, messages))
}


sealed trait InnerMessage
case class RoomMessage(user: String, msg: String)
case class ConnectMessage(name: String)
case class ConnectedMessage(name: String)
case class DisconnectMessage(name: String)
case class DisconnectedMessage(name: String)
