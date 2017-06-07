package models

import javax.inject.Singleton
import javax.inject.Inject
import slick.driver.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig

@Singleton
class UserRepository @Inject() (val provider: DatabaseConfigProvider) extends HasDatabaseConfig[JdbcProfile] with UsersTable {
  val dbConfig = provider.get[JdbcProfile]

  import dbConfig.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def findAll() = db
    .run(users.result)

  def find(id: Int) = db
    .run((for (user <- users if user.id === id) yield user).result.headOption)

  def findByApiKey(apiKey: String) = db
    .run((for (user <- users if user.apiKey === apiKey) yield user).result.headOption)

  def insert(user: User) = db
    .run(users returning users.map(_.id) += user)
    .map(id => user.copy(id = Some(id)))

  def remove(id: Int) = db
    .run(users.filter(_.id === id).delete) map { _ > 0 }

}