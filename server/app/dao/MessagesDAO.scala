package dao

import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import javax.inject.Inject
import models.Message
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
/**
  * Created by svloyso on 26.12.16.
  */
class MessagesDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Messages = TableQuery[MessagesTable]

  def all(): Future[Seq[Message]] = db.run(Messages.result)

  def allInRoom(room: String): Future[Seq[Message]] = db.run(Messages.filter(_.room === room).result)

  def lastTen(room: String): Future[Seq[Message]] = db.run(Messages.filter(_.room === room).sortBy(_.datetime).take(10).result)

  def insert(msg: Message): Unit = {
    val action = Messages ++= Seq(msg)
    db.run(action) map {res =>
      res map {x => x}
    }
  }

  private class MessagesTable(tag: Tag) extends Table[Message](tag, "messages") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def room = column[String]("room")
    def user = column[String]("user")
    def text = column[String]("text")
    def datetime = column[Long]("datetime")

    def * = (id.?, room, user, text, datetime) <> (Message.tupled, Message.unapply _)
  }
}
