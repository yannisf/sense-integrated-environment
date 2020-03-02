package fraglab

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.apache.camel.{Converter, TypeConverters}
import org.influxdb.dto.Point

class LineToPointConverter extends TypeConverters {

  private val measurement = ConfigFactory.load.getConfig("config").getString("influx.measurement")

  @Converter
  def toPoint(line: String): Point = {
    val parts = line.split(",")
    Point.measurement(measurement)
      .time(parts(0).toLong, TimeUnit.MILLISECONDS)
      .tag("room", parts(1))
      .tag("sensor", parts(2))
      .tag("serial", parts(3))
      .addField("humidity", parts(4).toDouble)
      .addField("temperature", parts(5).toDouble)
      .build()
  }

}
