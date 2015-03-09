
import java.util.Calendar
import play.api.Play

package object models {
  val CONNECTION_NAME = "folio"
  val DEFAULT_PAGE_SIZE = 50
  val DEFAULT_WAREHOUSE = 46
  val DEFAULT_YEAR = 2014
  def NOW = Calendar.getInstance.getTime

  object SqlDialect extends Enumeration {
    type SqlDialect = Value
    val SQL92 = Value("SQL92")
    val Oracle9 = Value("Oracle9")
    val MySQL = Value("MySQL")
    val H2 = Value("H2")
    val SQLServer2000 = Value("SQLServer2000")
    val SQLServer2005 = Value("SQLServer2005")
    val Informix = Value("Informix")
  }

  val SQLDialect = SqlDialect.withName(Play.current.configuration.getString(s"db.${CONNECTION_NAME}.dialect").getOrElse("SQL92"))
}