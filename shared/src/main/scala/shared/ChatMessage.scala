package shared

/**
  * Created by svloyso on 26.12.16.
  */
sealed trait ClientServerMessage
case class ClientMessage(user: String, text: String) extends ClientServerMessage
case class ServerMessage(user: String, text: String, datetime: String) extends ClientServerMessage
case class JoinMessage(user: String, datetime: String) extends ClientServerMessage
case class LeaveMessage(user: String, datetime: String) extends ClientServerMessage
case class UserlistMessage(users: Seq[String]) extends ClientServerMessage


object ClientServerMessage{
  import upickle.default._
  implicit val readWriter: ReadWriter[ClientServerMessage] =
    macroRW[UserlistMessage] merge macroRW[ClientMessage] merge macroRW[ServerMessage] merge macroRW[JoinMessage] merge macroRW[LeaveMessage]
}
