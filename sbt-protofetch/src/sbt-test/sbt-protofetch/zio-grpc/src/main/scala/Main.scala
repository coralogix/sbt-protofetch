import zio._
import io.opentelemetry.proto.logs.v1.logs.SeverityNumber

object Main extends ZIOAppDefault {
  def run = for {
    _ <- Console.printLine(SeverityNumber.SEVERITY_NUMBER_DEBUG.name)
  } yield ()
}
