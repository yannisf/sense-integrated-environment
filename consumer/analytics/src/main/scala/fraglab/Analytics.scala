package fraglab

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContextExecutor

object Analytics {

  implicit val system: ActorSystem = ActorSystem()
  implicit val ex: ExecutionContextExecutor = system.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer.create(system)

  implicit class DateFormat(value: String) {

    def toFormattedDate: String = {
      val df = new SimpleDateFormat("yyyyMMdd HH:mm:ss")
      df.format(new Date(value.toLong))
    }

    def toRoundedDouble: Double = {
      BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }
  }

  def main(args: Array[String]): Unit = {
    println("Hello")

    val config = system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("192.168.1.14:9092")
        .withGroupId("analytics.v1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .plainSource(consumerSettings, Subscriptions.topics("environment"))
      .map(_.value)
      .filter(_.contains("kids"))
      .filterNot(_.contains("None"))
      .map(_.split(","))
      .map{ array =>
        val date = array(0).toFormattedDate
        val hum = array(4).toRoundedDouble
        val temp = array(5).toRoundedDouble
        (date, hum, temp)
      }
      .mapConcat{ case (date, humidity, temperature ) => List((date, "hum", humidity), (date, "temp", temperature)) }
      .to(Sink.foreach(println))
      .run()
  }

}
