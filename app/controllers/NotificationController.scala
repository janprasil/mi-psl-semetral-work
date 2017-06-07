package controllers

import play.api.libs.streams.ActorFlow
import play.api.libs.json._
import services.UsersWebSocketActor
import models.User
import javax.inject.Inject
import javax.inject.Singleton
import akka.actor.ActorSystem
import play.api.mvc.Controller
import akka.stream.ActorMaterializer
import play.api.mvc.WebSocket
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.actor.Props
import akka.stream.Materializer
import play.api.mvc.Action
import play.api.http.ContentTypes
import play.api.libs.EventSource
import akka.stream.scaladsl.Source
import services.UsersSseActor
import play.api.libs.EventSource.EventDataExtractor

@Singleton
class NotificationController @Inject()(implicit val system: ActorSystem, materializer: Materializer) extends Controller {
  implicit val userFormat = Json.format[User]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[User, User]
  implicit val userEvents: EventDataExtractor[User] = EventDataExtractor(userFormat.writes(_).toString())
   
  def notifications = Action { request =>
    Ok(views.html.notifications(request))
  }
  
  def usersWs = WebSocket.accept[User, User] { request =>
    ActorFlow.actorRef(out => Props(new UsersWebSocketActor(out)))
  }
  
  def usersSse = Action {
    Ok.chunked(Source.actorPublisher(Props[UsersSseActor]) via EventSource.flow[User]).as(ContentTypes.EVENT_STREAM)
  }
}