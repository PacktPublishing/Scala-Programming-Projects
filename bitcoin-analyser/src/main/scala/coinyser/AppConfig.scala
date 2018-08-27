package coinyser

import java.net.URI

import scala.concurrent.duration.FiniteDuration

case class AppConfig(topic: String,
                     bootstrapServers: String,
                     firstInterval: FiniteDuration,
                     intervalBetweenReads: FiniteDuration,
                     transactionStorePath: URI)

