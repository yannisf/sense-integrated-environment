package fraglab

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.camel.Predicate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.SimpleRegistry
import org.apache.camel.support.builder.PredicateBuilder
import org.influxdb.{BatchOptions, InfluxDBFactory}

object HomeEnvironmentConsumer extends LazyLogging with App {

  val conf = ConfigFactory.load.getConfig("config")

  val influxDbBeanName = "influxDbBean"
  val influxDB = InfluxDBFactory.connect(conf.getString("influx.server"))
  private val batchOptions: BatchOptions = BatchOptions.DEFAULTS
    .actions(conf.getInt("influx.batchSize"))
    .flushDuration(conf.getInt("influx.flushMillis"))
    .bufferLimit(10000)
  influxDB.enableBatch(batchOptions)

  val registry = new SimpleRegistry()
  registry.bind(influxDbBeanName, influxDB)

  val context = new DefaultCamelContext(registry)
  context.getTypeConverterRegistry.addTypeConverters(new LineToPointConverter())
  context.addRoutes(new RouteBuilder() {

    private val fromKafka = s"kafka:${conf.getString("kafka.topic")}?" +
      s"brokers=${conf.getString("kafka.broker")}&" +
      s"groupId=${conf.getString("kafka.group")}&" +
      s"autoOffsetReset=earliest"

    private val toInfluxDb = s"influxdb://$influxDbBeanName?" +
      s"databaseName=${conf.getString("influx.dbname")}&" +
      s"retentionPolicy=autogen"

    val notContainsNone: Predicate = PredicateBuilder.not(bodyAs(classOf[String]).contains("None"))

    override def configure(): Unit =
      from(fromKafka)
        .log("Received: ${body}")
        .filter(notContainsNone)
        .to(toInfluxDb)
  })
  context.start()

  Thread.currentThread.join()
}
