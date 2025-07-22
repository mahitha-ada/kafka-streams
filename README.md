# Micronaut + Kafka Streams Demo

A working example of real-time stream processing using Micronaut and Kafka Streams. This project demonstrates how to build a word counting application that processes text messages in real-time.

## What's This About?

This demo shows how to combine Micronaut (a lightweight Java framework) with Kafka Streams to build applications that can process data in real-time. Instead of using heavy frameworks or complex distributed systems, everything runs as a single application.

The example processes text messages and counts word frequencies, but the same patterns can be applied to financial transactions, IoT sensor data, or any other real-time processing needs.

## Getting Started

You'll need Java 17+ and Docker installed.

**Start Kafka:**
```bash
docker compose up -d
```

**Run the app:**
```bash
cd kafka-streams-demo
./gradlew run
```

**Test it:**
```bash
# Send a message
curl -X POST http://localhost:8081/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "hello world hello kafka", "userId": "user123"}'

# Check word count
curl http://localhost:8081/api/wordcounts/hello
```

## What's Included

- **kafka-streams-demo/**: The main application code
- **docker-compose.yml**: Local Kafka setup
- **TESTING.md**: How to test everything properly
- **TROUBLESHOOTING.md**: Common issues and fixes
- **images/**: Architecture diagrams

## Key Features

- REST API for sending messages and querying results
- Real-time word counting with Kafka Streams
- State stores for fast queries
- Docker setup for easy local development
- Comprehensive testing and troubleshooting guides

## Technology Stack

- Micronaut 4.x for the web framework
- Kafka Streams for real-time processing
- Apache Kafka for messaging
- Java 17
- Gradle for builds

## Testing

Check out `TESTING.md` for detailed testing instructions including API examples, load testing, and automated test scripts.

## Troubleshooting

If you run into issues, `TROUBLESHOOTING.md` has solutions for common problems like state store corruption, memory issues, and Kafka connection problems.

## Architecture

The application uses a simple architecture where REST controllers handle HTTP requests, services manage Kafka interactions, and Kafka Streams processes the data in real-time. Check the `images/` folder for visual diagrams.

## Extending This

This is a basic word counting example, but you can extend it for:
- Time-windowed analytics
- Stream joins between multiple data sources
- More complex aggregations
- Integration with external systems

## License

Feel free to use this as a starting point for your own projects.

---

*Built by Mahitha Adapa*

