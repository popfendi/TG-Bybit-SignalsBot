# TG / Bybit trading signals bot

I got commissioned to create this TG bot for a trading signals channel before they decided to go awol and not pay me (that's my excuse for no tests). So sharing it here for anyone that may find it useful.

The bot must be an admin of the channel in order for it to work. 

## How it works

You send a message to your channel in this format: 

```agsl
🔮🤖🔮🤖🔮🤖 (emojis optional)

Pair: BTCUSDT <-- the pair as it is written on bybit
Direction: Long 

Entry: 27025 <- entry price

TP1: 27100 <-- price targets
TP2: 27200
TP3: 27300

SL: 26950 <-- stop loss
```
The bot parses this message, stores the open trade in a mongoDB and subscribes to price updates from the bybit api.

It then tracks the targets, sends updates to the channel when the targets are hit & keeps track of trading stats (these are available through commands).

## Usage

1. ```mvn clean install``` to build the .jar (you can run standalone with just the jar, fill out the application.properties with your config and make sure you have a mongoDB running somewhere).
2.  for docker, place the .jar in the docker folder and run ```docker build . --tag=signal-bot``` inside that folder.
3.  you can then fill out the docker-compose.yaml with your config & and ```docker-compose up -d```

### Project Structure

```
├── docker  
│   ├── docker-compose.yaml 
│   └── dockerfile
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── popfendi
│   │   │           ├── bots
│   │   │           │   └── SignalBot.java
│   │   │           ├── client
│   │   │           │   ├── BybitWebsocket.java 
│   │   │           │   └── Client.java -- handles conn to bybit
│   │   │           ├── config
│   │   │           │   ├── ArgsParser.java -- parses cmd line args
│   │   │           │   └── PropertiesLoader.java -- loads external props
│   │   │           ├── handlers
│   │   │           │   ├── EarningsCommand.java -- handler for /earnings cmd
│   │   │           │   ├── ListCommand.java -- handler for /list command
│   │   │           │   ├── MessageHandler.java -- handler for non command msgs
│   │   │           │   ├── StatsCommand.java -- hander for /stats command
│   │   │           │   └── StreamHandler.java -- handler for data stream from bybit
│   │   │           ├── Main.java 
│   │   │           ├── models
│   │   │           │   ├── Direction.java
│   │   │           │   ├── PriceData.java 
│   │   │           │   ├── Signal.java
│   │   │           │   ├── Stats.java
│   │   │           │   └── Targets.java
│   │   │           └── repository
│   │   │               ├── DataManager.java -- holds most of the data handling logic
│   │   │               └── MongoDBClient.java -- db client
│   │   └── resources
│   │       ├── application.properties -- config
│   │       ├── delta.json -- example event from bybit
│   │       ├── fail.json -- example event from bybit
│   │       └── snapshot.json -- example event from bybit
│   └── test
│       
└── target

```

I've tried to comment the code in a clear and concise way to read through the code and it should make sense.

