import io.opentelemetry.proto.logs.v1.logs.SeverityNumber

object Main {
  def main(args: Array[String]) = {
    println(SeverityNumber.SEVERITY_NUMBER_DEBUG.name)
  }
}
