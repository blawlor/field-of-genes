package ie.blawlor.worker

import akka.actor._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import ie.blawlor.fieldofgenes.RefSeqLoader
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.slf4j.LoggerFactory
import scopt.OptionParser


class SettingsImpl(config: Config) extends Extension {
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup() = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)

  override def get(system: ActorSystem): SettingsImpl = super.get(system)
}

object Main {
  val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val MAX_CONCURRENT_PRODUCERS = 1

  case class Parameters(kafkaPort: Int = 9092,
                        kafkaHost: String = "127.0.0.1")

  val parser = new OptionParser[Parameters]("kgd") {
    head("kgd")
    opt[Int]('j', "kafka-port") action {(x,c) =>
      c.copy(kafkaPort = x)} text "The kafka port number to use"
    opt[String]('k', "kafka-hostname") action {(x,c) =>
      c.copy(kafkaHost = x)} text "The kafka hostname to use"
  }

  def main(args: Array[String]): Unit = {
    val agentid = sys.env.getOrElse("HOSTNAME", "Unknown")
    parser.parse(args, Parameters()) match {
      case Some(parameters) =>
        val kafkaPort = parameters.kafkaPort
        val kafkaHost = parameters.kafkaHost
        // Override the configuration of the port when specified as program argument
        val config = ConfigFactory.parseString(s"kgd.kafka.port = $kafkaPort").
          withFallback(ConfigFactory.parseString(s"kgd.kafka.host = $kafkaHost")).
          withFallback(ConfigFactory.load())

        implicit val system = ActorSystem("kgd", config)
        implicit val materializer = ActorMaterializer()

        logger.warn(s"Agent $agentid is about to create a consumer of kgd-load topic on $kafkaHost server using port $kafkaPort")
        val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
          .withBootstrapServers(kafkaHost+":"+kafkaPort)
          .withGroupId("group1")
          .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
          .withBootstrapServers(kafkaHost+":"+kafkaPort)
        val subscription = Subscriptions.topics("kgd-load")

        Consumer.committableSource(consumerSettings, subscription)
          .map { committableMessage =>
            logger.warn(s"Doing work with ${committableMessage.record.value()}")
            val resultString = performWork(committableMessage.record.value, kafkaHost, kafkaPort)
            ProducerMessage.Message(new ProducerRecord[String, String](
              "kgd-load-res",
              resultString), committableMessage.committableOffset)
          }
          .runWith(Producer.commitableSink(producerSettings))
      case None =>
    }
  }

  def performWork(instruction: String, kafkaHost: String, kafkaPort: Int):String= {
    RefSeqLoader.load(instruction, "ref-seq", kafkaHost + ":" + kafkaPort)
  }
}
