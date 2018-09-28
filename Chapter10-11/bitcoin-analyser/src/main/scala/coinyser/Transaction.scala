package coinyser

import java.sql.{Date, Timestamp}
import java.time.ZoneOffset

case class Transaction(timestamp: Timestamp,
                       date: Date,
                       tid: Int,
                       price: Double,
                       sell: Boolean,
                       amount: Double)


object Transaction {
  def apply(timestamp: Timestamp,
            tid: Int,
            price: Double,
            sell: Boolean,
            amount: Double) =
    new Transaction(
      timestamp = timestamp,
      date = Date.valueOf(
        timestamp.toInstant.atOffset(ZoneOffset.UTC).toLocalDate),
      tid = tid,
      price = price,
      sell = sell,
      amount = amount)
}
