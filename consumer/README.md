# Home consumer

Has the responsibility to consume from Kafka all the environment information lines sent by the sensors and insert them into InfluxDB.

## Build

    $ sbt universal:packageBin
    $ cp target/universal/home-consumer.x.y.z.zip to_location
    ... unzip
    $ bin/home-consumer