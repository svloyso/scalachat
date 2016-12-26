package actors
import akka.actor._
import play.api.Logger

import scala.collection.mutable

/**
  * Created by svloyso on 26.12.16.
  */
class RoomActor extends Actor {
  var listeners: mutable.MutableList[ActorRef] = new mutable.MutableList()
  def receive = {
    case ConnectMessage(name) =>
      Logger.info(s"Got an connect message from $name")
      listeners += sender()
      listeners.map {out =>
        out ! ConnectedMessage(name)
      }
    case RoomMessage(user, msg) =>
      listeners.map {out =>
        out ! RoomMessage(user, msg)
      }
    case DisconnectMessage(name) =>
      Logger.info(s"Got an disconnect message from $name")
      listeners = listeners diff Seq(sender())
      listeners.map {out =>
        out ! DisconnectedMessage(name)
      }
  }
}

object RoomActor {
  def props = Props(new RoomActor)
}


sealed trait InnerMessage
case class RoomMessage(user: String, msg: String)
case class ConnectMessage(name: String)
case class ConnectedMessage(name: String)
case class DisconnectMessage(name: String)
case class DisconnectedMessage(name: String)
