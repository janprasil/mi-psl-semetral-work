package services

import javax.inject.Singleton
import javax.inject.Inject
import scala.concurrent.Future
import models._
import akka.actor.ActorSystem
import scala.util.Success

@Singleton
class UserService @Inject() (val repository: UserRepository, system: ActorSystem) {
  import scala.concurrent.ExecutionContext.Implicits.global
  
  def findAll(): Future[Seq[User]] = repository.findAll
  def findByApiKey(apiKey: String): Future[Option[User]] = repository.findByApiKey(apiKey)
  def insert(user: User): Future[User] = repository
    .insert(user)
    .andThen {
      case Success(user) => system.eventStream.publish(UserAdded(user))
    }
}