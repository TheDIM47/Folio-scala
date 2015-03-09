package utils

import scala.collection.Seq

case class Page[A](items: Seq[A], page: Int, offset: Int, total: Int) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}