package controllers

import javax.inject._
import actors._
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import dao.MessagesDAO
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.Logger
import slick.driver.JdbcProfile
import scala.collection.mutable

@Singleton
class Application @Inject()(implicit system: ActorSystem,
                            materializer: Materializer,
                            msgDAO: MessagesDAO) extends Controller {

  val rooms: mutable.HashMap[String, ActorRef] = new mutable.HashMap()

  def index = Action { implicit request =>
    Ok(views.html.index("Scala Chat"))
  }

  def chatWS = WebSocket.accept[String, String] { request =>
    val user = request.queryString.get("user").flatMap(_.headOption).getOrElse("")
    val room = request.queryString.get("room").flatMap(_.headOption).getOrElse("")
    Logger.info(s"Establish websocket connection from user $user in room $room")
    if(!rooms.contains(room)) {
      rooms(room) = system.actorOf(RoomActor.props(room, msgDAO))
    }
    ActorFlow.actorRef(out => UserActor.props(user, out, rooms(room)))
  }

}
