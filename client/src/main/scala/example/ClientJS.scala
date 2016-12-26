package example

import shared._

import scala.scalajs.js
import org.scalajs.dom
import scalatags.JsDom._
import all._
import org.scalajs.jquery.{jQuery=>$}
import upickle.default._

object ClientJS extends js.JSApp {
  val socketURL = $("body").data("ws-url").toString
  val maxMessages = 20

  var socket: Option[dom.WebSocket] = None
  var user: String = ""
  var room: String = ""
  val body = $("#content")


  def setupUI(): Unit = {
    body.append(signInPanel.render)
    body.append(chatPanel.render)
    $("#message").keypress((e: dom.KeyboardEvent) => {
      if(!e.shiftKey && e.keyCode == 13) {
        e.preventDefault()
        socket.foreach{_.send(write(ClientMessage(user, $("#message").value().toString)))}
        $("#message").value("")
      }
    })
  }


  def receive(e: dom.MessageEvent) = {
    val msgStr = e.data.toString
    dom.console.log("Got an message: " + msgStr)
    val msg: ClientServerMessage = read[ClientServerMessage](msgStr)
    val msgElem = dom.document.getElementById ("messages")
    msg match {
      case ServerMessage(username, text, dt) =>
        msgElem.appendChild (postMessage (text, username, dt).render)
      case JoinMessage(username, dt) =>
        msgElem.appendChild (postMessage (s"$username has joined to chat", "*", dt).render)
        $("#users").append(
          li(id:=username)(username).render
        )
      case LeaveMessage(username, dt) =>
        msgElem.appendChild (postMessage (s"$username left", "*", dt).render)
        $(s"#$username").remove()
      case UserlistMessage(users) =>
        users.foreach{u => $("#users").append(li(id:=u)(u).render)}
      case other => dom.console.error("Got a bad message: " + other.toString)
    }
    if (msgElem.childNodes.length >= maxMessages) {
      msgElem.removeChild (msgElem.firstChild)
    }
    msgElem.scrollTop = msgElem.scrollHeight}

  def signInPanel = div(id:="signInPanel"){
    form(`class`:="form-inline")(
      div(id:="usernameForm", `class`:="form-group")(
        div(`class`:="input-group")(
          div(`class`:="input-group-addon", raw("&#9786;")),
          input(id:="username", `class`:="form-control", `type`:="text", placeholder:="Enter username"),
          input(id:="chatroom", `class`:="form-control", `type`:="text", placeholder:="Enter chatroom")
        )
      ),
      span(style:="margin:0px 5px"),
      button(`class`:="btn btn-default", onclick:={ () =>
        user = $("#username").value().toString.trim
        room = $("#chatroom").value().toString.trim
        if(user == "" || room == "") {
          $("#usernameForm").addClass("has-error")
          dom.window.alert("Username and chatroom can not be empty")
        } else {
          $("#usernameForm").removeClass("has-error")
          $("#signInPanel").addClass("hide")
          $("#chatPanel").removeClass("hide")
          $("#chatTitle").html(room)
          socket = try {
            val s = new dom.WebSocket(socketURL + "?room=" + room + "&user=" + user)
            s.onmessage = receive _
            Some(s)
          } catch { case e: Throwable =>
            dom.window.alert("Can not connect to server socket because " + e.toString)
            None
          }
        }
        false
      })("Sign in")
    )
  }

  def chatPanel = div(id:="chatPanel", `class`:="hide")(
    div(`class`:="row", style:="margin-bottom: 10px;")(
      div(`class`:="col-md-12", style:="text-align: right;")(
        span(id:="loginAs", style := "padding: 0px 10px;"),
        button(`class`:="btn btn-default", onclick:={ () =>
          signOut
        }, "Sign out")
      )
    ),
    div(`class` := "panel panel-default")(
      div(`class` := "panel-heading")(
        h3(id := "chatTitle", `class` := "panel-title")("Chat Room")
      ),
      div(`class` := "panel-body")(
        div(id := "messages")
      ),
      div(`class` := "panel-footer")(
        textarea(id:="message", `class` := "form-control message", placeholder := "Say something")
      )
    )
  )

  def postMessage(msg: String, username: String, datetime: String) = {
    div(`class`:=s"row message-box${if(username == user)"-me" else ""}")(
      div(`class`:="col-md-2")(
        div(`class`:="message-username")(
          div(username)
        )
      ),
      div(`class`:="col-md-10")(
        div(`class`:="row")(
          div(`class`:="message-datetime")(
            div(datetime)
          ),
          raw(msg)
        )
      )
    )
  }

  def signOut = {
    socket.map(_.close())
    $("#signInPanel").removeClass("hide")
    $("#chatPanel").addClass("hide")
    $("#messages").html("")
  }

  def main(): Unit = {
    setupUI()
  }
}