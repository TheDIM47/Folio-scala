package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.cache.Cache
import play.api.db.DB

//case class Manager(name:String)

object Manager {
  val DATA_KEY = "list.managers"
  val EXPIRATION_SEC = 600

  def list: Seq[String] = {
    Cache.getOrElse(DATA_KEY) {
      val result = DB.withConnection(CONNECTION_NAME) { implicit c =>
        val sql = "select distinct upper(CP_2) as name from _PARTNER where NULLIF(CP_2,'') is not null order by upper(CP_2)"
        SQL(sql).as(str("name") *)
//        ().collect {
//          case Row(name:Option[String]) => Manager(name.getOrElse(""))
//        }
      }
      Cache.set(DATA_KEY, result, EXPIRATION_SEC)
      result
    }
  }
}
