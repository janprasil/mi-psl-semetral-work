package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models.{Post, PostRepository}

import scala.concurrent.Future
import scala.util.Success

@Singleton
class PostService @Inject() (val repository: PostRepository, system: ActorSystem) {
  import scala.concurrent.ExecutionContext.Implicits.global

  def findAll(): Future[Seq[Post]] = repository.findAll
  def find(id: Int): Future[Option[(models.Post, models.User)]] = repository.find(id)
  def remove(id: Int, apiKey: String): Future[Boolean] = repository.remove(id, apiKey)
  def insert(post: Post): Future[Post] = repository
    .insert(post)
}