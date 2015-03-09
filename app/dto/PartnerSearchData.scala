package dto

import play.api.data.Form
import play.api.data.Forms._
import java.util.Date

case class PartnerSearchData(
  manager: Option[String],
  date: Option[Date],
  ware: Int = models.DEFAULT_WAREHOUSE,
  year: Int = models.DEFAULT_YEAR,
  psize: Int = models.DEFAULT_PAGE_SIZE
)

object PartnerSearchData {
  val form = Form(
    mapping(
      "manager" -> optional(text),
      "date" -> optional(date("dd.MM.yyyy")),
      "ware" -> default(number, models.DEFAULT_WAREHOUSE),
      "year" -> default(number, models.DEFAULT_YEAR),
      "psize" -> default(number, models.DEFAULT_PAGE_SIZE)
    )(PartnerSearchData.apply)(PartnerSearchData.unapply)
  )
}