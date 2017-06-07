package models

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

case class User(id: Option[Int],
                email: String,
                firstName: Option[String],
                lastName: Option[String],
                apiKey: Option[String]
               )

trait UsersTable { this: HasDatabaseConfig[JdbcProfile] =>
  import dbConfig.driver.api._

  private[models] class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Int]("USER_ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("USER_EMAIL", O.Length(512))
    def firstName = column[Option[String]]("USER_FIRST_NAME", O.Length(64))
    def lastName = column[Option[String]]("USER_LAST_NAME", O.Length(64))
    def apiKey = column[Option[String]]("API_KEY", O.Length(128))

    def emailIndex = index("USER_EMAIL_IDX", email, true)

    def * = (id.?, email, firstName, lastName, apiKey) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]
}

