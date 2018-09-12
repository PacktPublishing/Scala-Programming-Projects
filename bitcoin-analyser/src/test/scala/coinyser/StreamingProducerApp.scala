package coinyser

import cats.effect.{ExitCode, IO, IOApp}
import com.pusher.client.Pusher

// Use log compaction to ensure high availability ? (2 producers running concurrently)
object StreamingProducerApp extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val config = KafkaConfig("localhost:9092", "transactions_draft4")

    val pusher = new Pusher("de504dc5763aeef9ff52")
    StreamingProducer.start(pusher, config).map(_ => ExitCode.Success)

  }
}




