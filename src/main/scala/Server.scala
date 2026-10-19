import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*

/** Multi-client chat server. Every connected client is served by its own thread. */
object Server:

  // username -> handler, safe to use from many threads at once
  private val clients = new ConcurrentHashMap[String, ClientHandler]()

  def main(args: Array[String]): Unit =
    val port = args.headOption.map(_.toInt).getOrElse(5000)
    val serverSocket = new ServerSocket(port)
    println(s"ScalaChat server started on port $port")
    while true do
      val socket = serverSocket.accept()          // wait for a new client
      new Thread(new ClientHandler(socket)).start() // one thread per client

  /** Send a message to every connected user, optionally skipping one. */
  private def broadcast(message: String, except: Option[String] = None): Unit =
    clients.asScala.foreach { (name, handler) =>
      if !except.contains(name) then handler.send(message)
    }

  private class ClientHandler(socket: Socket) extends Runnable:
    private val in  = new BufferedReader(new InputStreamReader(socket.getInputStream))
    private val out = new PrintWriter(socket.getOutputStream, true)
    private var username: String = ""

    def send(message: String): Unit = out.println(message)

    override def run(): Unit =
      try
        if register() then
          chatLoop()
      catch
        case _: java.io.IOException => () // client dropped the connection
      finally
        cleanup()

    /** Ask for a unique username. Returns false if the client disconnects first. */
    private def register(): Boolean =
      send("Enter your username:")
      var done = false
      while !done do
        val line = in.readLine()
        if line == null then return false
        val name = line.trim
        if name.isEmpty || name.exists(_.isWhitespace) then
          send("Username cannot be empty or contain spaces. Try again:")
        else if clients.putIfAbsent(name, this) != null then
          send("That username is taken. Try another:")
        else
          username = name
          done = true
      send(s"Welcome, $username! Commands: /users, /msg <user> <text>, /quit")
      broadcast(s"* $username joined the chat", except = Some(username))
      println(s"$username connected")
      true

    private def chatLoop(): Unit =
      var running = true
      while running do
        val line = in.readLine()
        if line == null then running = false
        else line.trim match
          case ""        => ()
          case "/quit"   => running = false
          case "/users"  => send("Online: " + clients.keySet.asScala.toList.sorted.mkString(", "))
          case cmd if cmd.startsWith("/msg ") => privateMessage(cmd.drop(5))
          case text      => broadcast(s"$username: $text", except = Some(username))

    private def privateMessage(rest: String): Unit =
      rest.split(" ", 2) match
        case Array(target, text) if text.trim.nonEmpty =>
          Option(clients.get(target)) match
            case Some(h) =>
              h.send(s"[private] $username: ${text.trim}")
              send(s"[private to $target] ${text.trim}")
            case None => send(s"User '$target' is not online.")
        case _ => send("Usage: /msg <user> <text>")

    private def cleanup(): Unit =
      if username.nonEmpty && clients.remove(username, this) then
        broadcast(s"* $username left the chat")
        println(s"$username disconnected")
      socket.close()
