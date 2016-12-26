package models

/**
  * Created by svloyso on 26.12.16.
  */
case class Message(id: Option[Long], room: String, user: String, text: String, dateTime: Long)
