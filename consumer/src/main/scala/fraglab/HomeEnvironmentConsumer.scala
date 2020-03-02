package fraglab

import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import org.influxdb.dto.Point
import org.influxdb.{BatchOptions, InfluxDBFactory}

import scala.collection.JavaConverters._

object HomeEnvironmentConsumer extends LazyLogging with App {

  val conf = ConfigFactory.load.getConfig("config")
  val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, conf.getString("kafka.broker"))
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, conf.getString("kafka.group"))

  val consumer = new KafkaConsumer(props)
  val topics = List(conf.getString("kafka.topic"))

  val influxDB = InfluxDBFactory.connect(conf.getString("influx.server"))
  val dbName = conf.getString("influx.dbname")
  influxDB.setDatabase(dbName)
  influxDB.enableBatch(BatchOptions.DEFAULTS)

  try {
    consumer.subscribe(topics.asJava)
    while (true) {
      val records = consumer.poll(Duration.ofSeconds(10L))
      for (record <- records.asScala) {
        val value = record.value.toString.split(",")
        logger.info(s"$value")
        val timestamp = value(0).toLong
        val room = value(1)
        val sensor = value(2)
        val serial = value(3)
        val humidityRaw = value(4)
        val temperatureRaw = value(5)

        if (humidityRaw != "None" && temperatureRaw != "None") {
          influxDB.write(Point.measurement(conf.getString("influx.measurement"))
            .time(timestamp, TimeUnit.MILLISECONDS)
            .tag("room", room)
            .tag("sensor", sensor)
            .tag("serial", serial)
            .addField("humidity", humidityRaw.toDouble)
            .addField("temperature", temperatureRaw.toDouble)
            .build())
        }
      }
    }
  } catch {
    case e: Exception => logger.error("Error: ", e)
  } finally {
    consumer.close()
    influxDB.close()
  }
}
