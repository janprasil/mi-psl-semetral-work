package services

import models.User

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef

case class UserAdded(user: User)

class UsersWebSocketActor(val out: ActorRef) extends Actor with ActorLogging {
  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[UserAdded])
    log.info("Subscribed to user notifications")
  }
  
  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
    log.info("Unsubscribed from user notifications")
  }
  
  def receive() = {
    case UserAdded(user) => out ! user
  }
}