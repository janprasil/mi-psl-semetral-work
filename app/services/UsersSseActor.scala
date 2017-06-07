package services

import akka.stream.actor.ActorPublisher
import models.User
import akka.stream.actor.ActorPublisherMessage.Request
import akka.stream.actor.ActorPublisherMessage.Cancel
import akka.actor.ActorLogging
import scala.annotation.tailrec

/**
 * http://doc.akka.io/docs/akka/2.4.10/scala/stream/stream-integrations.html
 */
class UsersSseActor extends ActorPublisher[User] with ActorLogging {
  var buffer = Vector.empty[User]
  
  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[UserAdded])
    log.info("Subscribed to user notifications")
  }
  
  override def postStop() = {
    context.system.eventStream.unsubscribe(self)
    log.info("Unsubscribed from user notifications")
  }
  
  def receive = {
    case UserAdded(user) if buffer.size < 100 => {
        buffer :+= user
        send()
      }
    
    case Request(_) => send()
    case Cancel => context.stop(self)
  }
  
  private[this] def send(): Unit = if (totalDemand > 0) {
    val (use, keep) = buffer.splitAt(totalDemand.toInt)
    buffer = keep
    use foreach onNext
  }
}