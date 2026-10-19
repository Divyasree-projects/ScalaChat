import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket

/** Console chat client. One thread prints incoming messages, the main thread sends yours. */
object Client:

  def main(args: Array[String]): Unit =
    val host = if args.length > 0 then args(0) else "localhost"
    val port = if args.length > 1 then args(1).toInt else 5000

    val socket = new Socket(host, port)
    val in     = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val out    = new PrintWriter(socket.getOutputStream, true)

    // Receiver thread: shows messages from the server in real time
    val receiver = new Thread(new Runnable:
      override def run(): Unit =
        try
          var line = in.readLine()
          while line != null do
            println(line)
            line = in.readLine()
        catch case _: java.io.IOException => ()
        println("Disconnected from server.")
        Runtime.getRuntime.halt(0)
    )
    receiver.setDaemon(true)
    receiver.start()

    // Main thread: read from the keyboard and send to the server
    val keyboard = new BufferedReader(new InputStreamReader(System.in))
    var input = keyboard.readLine()
    while input != null do
      out.println(input)
      if input.trim == "/quit" then
        socket.close()
        sys.exit(0)
      input = keyboard.readLine()
    socket.close()
