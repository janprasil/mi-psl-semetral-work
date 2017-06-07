package models

import javax.inject.Singleton
import javax.inject.Inject
import slick.driver.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

@Singleton
class PostRepository @Inject() (val provider: DatabaseConfigProvider) extends HasDatabaseConfig[JdbcProfile] with PostsTable {
  val dbConfig = provider.get[JdbcProfile]

  import dbConfig.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def findAll() = db.run(posts.result)

  def find(id: Int) = db.run(
    (for ((post, user) <- posts join users if (post.userId === user.id && post.id === id))
      yield (post, user)).result.headOption)

  def insert(post: Post) = db
    .run(posts returning posts.map(_.id) += post)
    .map(id => post.copy(id = Some(id)))

  def remove(id: Int, apiKey: String) =
    db.run(posts.filter(_.id === id).filter(_.userFK.filter(x => x.apiKey === apiKey).size === 1).delete) map { _ > 0 }

}
