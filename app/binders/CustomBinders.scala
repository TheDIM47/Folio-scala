package binders

//import models.TaskSearchData

import java.util.Date
import java.text.SimpleDateFormat

import dto.{NaklSearchData, PartnerSearchData}
//import org.joda.time.DateTime
//import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.mvc.QueryStringBindable

object CustomBinders {

  def toOption[A](v:Option[Either[String, A]], default:Option[A] = None): Option[A] = v match {
    case Some(x) => x match {
      case Left(x) => default
      case Right(x) => Some(x)
    }
    case _ => default
  }
  def toValue[A](v:Option[Either[String, A]], default:A): A = v match {
    case Some(x) => x match {
      case Left(x) => default
      case Right(x) => x
    }
    case _ => default
  }

  implicit def naklSearchDataBinder(implicit intBinder: QueryStringBindable[Int],
                                       strBinder: QueryStringBindable[String]): QueryStringBindable[NaklSearchData] = new QueryStringBindable[NaklSearchData] {

    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, NaklSearchData]] = {
      Some(Right(NaklSearchData(
        partner = toValue(strBinder.bind(key + ".partner", params), "%"), // partner,
        date = toValue(jdateBinder.bind(key + ".date", params), models.NOW), // date,
        ware = toValue(intBinder.bind(key + ".ware", params), models.DEFAULT_WAREHOUSE), // ware,
        year = toValue(intBinder.bind(key + ".year", params), models.DEFAULT_YEAR) // year,
      )))
    }

    def unbind(key: String, v: NaklSearchData): String = {
      val sb = StringBuilder.newBuilder
      sb.append(key + ".partner=" + v.partner)
      sb.append("&" + key + ".date=" + jdateBinder.unbind(key, v.date))
      sb.append("&" + key + ".ware=" + v.ware)
      sb.append("&" + key + ".year=" + v.year)
      sb.toString
    }
  }

  implicit def partnerSearchDataBinder(implicit intBinder: QueryStringBindable[Int],
                                       strBinder: QueryStringBindable[String]): QueryStringBindable[PartnerSearchData] = new QueryStringBindable[PartnerSearchData] {

    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PartnerSearchData]] = {
        Some(Right(PartnerSearchData(
          psize = toOption(intBinder.bind(key + ".psize", params)).getOrElse(models.DEFAULT_PAGE_SIZE),
          manager = toOption(strBinder.bind(key + ".manager", params)), // manager,
          date = toOption(jdateBinder.bind(key + ".date", params)), // date,
          ware = toValue(intBinder.bind(key + ".ware", params), models.DEFAULT_WAREHOUSE), // ware,
          year = toValue(intBinder.bind(key + ".year", params), models.DEFAULT_YEAR) // year,
        )))
    }

    def unbind(key: String, v: PartnerSearchData): String = {
      val sb = StringBuilder.newBuilder
      sb.append(key + ".psize=" + v.psize)
      if (v.manager != None) sb.append("&" + key + ".manager=" + v.manager.get)
      if (v.date != None) sb.append("&" + key + ".date=" + jdateBinder.unbind(key, v.date.get))
      sb.append("&" + key + ".ware=" + v.ware)
      sb.append("&" + key + ".year=" + v.year)
      sb.toString
    }
  }

  implicit def jdateBinder: QueryStringBindable[Date] = new QueryStringBindable[Date] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Date]] = {
//      val dateTimeFormatter: SimpleDateFormat = new SimpleDateFormat("dd.MM.yyyy")
      val dateString: Option[Seq[String]] = params.get(key)
      try {
        dateString match {
          case Some(x) => Some(Right(new SimpleDateFormat("dd.MM.yyyy").parse(x.head)))
          case _ => None
        }
      } catch {
        case e: IllegalArgumentException => Option(Left(dateString.get.head))
      }
    }

    def unbind(key: String, value: Date): String = {
//      val dateTimeFormatter: SimpleDateFormat = new SimpleDateFormat("dd.MM.yyyy")
      new SimpleDateFormat("dd.MM.yyyy").format(value)
    }
  }

//  val jodaBinder = jodaDateTimeBinder
//
//  /**
//   * Implicit conversions for org.joda.time.DateTime
//   * @return QueryStringBindable[org.joda.time.DateTime]
//   */
//  implicit def jodaDateTimeBinder: QueryStringBindable[DateTime] = new QueryStringBindable[DateTime] {
//    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, DateTime]] = {
//      val dateString: Option[Seq[String]] = params.get(key)
//      try {
//        dateString match {
//          case Some(x) => Some(Right(dateTimeFormatter.parseDateTime(x.head)))
//          case _ => None
//        }
//      } catch {
//        case e: IllegalArgumentException => Option(Left(dateString.get.head))
//      }
//    }
//
//    def unbind(key: String, value: DateTime): String = {
//      dateTimeFormatter.print(value)
//    }
//  }
//
//  implicit def jodaDateTimeBinder2: QueryStringBindable[Option[DateTime]] = new QueryStringBindable[Option[DateTime]] {
//    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Option[DateTime]]] = {
//      val dateString: Option[Seq[String]] = params.get(key)
//      try {
//        Some(Right(Some(dateTimeFormatter.parseDateTime(dateString.get.head))))
//      } catch {
//        case e: IllegalArgumentException => Option(Left(dateString.get.head))
//      }
//    }
//
//    def unbind(key: String, value: Option[DateTime]): String = {
//      value match {
//        case Some(dt) => dateTimeFormatter.print(dt)
//        case _ => ""
//      }
//    }
//  }


}
