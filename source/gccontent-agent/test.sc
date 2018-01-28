import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
implicit val system = ActorSystem("application")
implicit val materializer = ActorMaterializer()
Source(1 to 10)
    .map{ whatever =>
      println(whatever)
    }
  .runWith(Sink.ignore)
