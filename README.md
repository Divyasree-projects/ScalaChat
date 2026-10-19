# ScalaChat

A multi-user, real-time chat application written in **Scala 3** using plain TCP sockets.
A server accepts many clients at the same time, and every client is handled by its own thread.

## Features
- Client-server architecture over TCP sockets
- Real-time messaging between all connected users
- Multithreading: one thread per client on the server, a separate receiver thread on each client
- Unique usernames with validation
- Private messages with `/msg <user> <text>`
- `/users` to list who is online, `/quit` to leave
- Join and leave notifications
- Thread-safe user list (`ConcurrentHashMap`)

## Tech stack
Scala 3, Java sockets and threads, sbt (optional)

## Project structure
```
ScalaChat/
├── build.sbt
└── src/main/scala/
    ├── Server.scala   # accepts connections, one thread per client, broadcasts messages
    └── Client.scala   # console client: receiver thread + keyboard input
```

## How to run

### Option 1: with sbt
```bash
# Terminal 1: start the server (default port 5000)
sbt "runMain Server"

# Terminal 2 and 3: start clients
sbt "runMain Client localhost 5000"
```

### Option 2: with the Scala compiler
```bash
mkdir out
scalac -d out src/main/scala/*.scala
scala -classpath out Server                  # server
scala -classpath out Client localhost 5000   # each client, in its own terminal
```

## Example session
```
Enter your username:
alice
Welcome, alice! Commands: /users, /msg <user> <text>, /quit
* bob joined the chat
bob: hello alice
/msg bob see you soon
[private to bob] see you soon
```

## Commands
| Command | What it does |
|---|---|
| `/users` | Show online users |
| `/msg <user> <text>` | Send a private message |
| `/quit` | Disconnect |

## Possible improvements
- Chat rooms and message history
- GUI client
- Encrypted connections (TLS)

## Author
Divya Sree T
