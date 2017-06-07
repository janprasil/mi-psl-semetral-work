package models

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

case class Post(id: Option[Int],
                title: String,
                description: Option[String],
                text: String,
                userId: Option[Int]
               )

trait PostsTable extends UsersTable { this: HasDatabaseConfig[JdbcProfile] =>
  import dbConfig.driver.api._

  private[models] class Posts(tag: Tag) extends Table[Post](tag, "POSTS") {

    def id = column[Int]("POST_ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("POST_TITLE", O.Length(512))
    def description = column[Option[String]]("POST_DESCRIPTION", O.Length(512))
    def text = column[String]("POST_TEXT", O.Length(2048))
    def userId = column[Option[Int]]("POST_USER_ID")

    def userFK = foreignKey("USER_FK", userId, users)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

    def * = (id.?, title, description, text, userId) <> (Post.tupled, Post.unapply)
  }

  val posts = TableQuery[Posts]
}

