package dto

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

case class NaklSearchData (
  partner: String,
  date: Date = models.NOW,
  ware: Int = models.DEFAULT_WAREHOUSE,
  year: Int = models.DEFAULT_YEAR
//  psize: Int = models.DEFAULT_PAGE_SIZE
)

object NaklSearchData {
  val form = Form(
    mapping(
      "partner" -> text(8, 8),
      "date" -> default(date("dd.MM.yyyy"), models.NOW),
      "ware" -> default(number, models.DEFAULT_WAREHOUSE),
      "year" -> default(number, models.DEFAULT_YEAR)
//      "psize" -> default(number, models.DEFAULT_PAGE_SIZE)
    )(NaklSearchData.apply)(NaklSearchData.unapply)
  )
}