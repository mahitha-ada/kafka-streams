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

**Create required Kafka topics:**

```bash
# Create input topic
docker exec -it kafka kafka-topics --create \
  --topic text-messages \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Create output topic
docker exec -it kafka kafka-topics --create \
  --topic word-counts \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

**If you encounter state store errors, clean the state directory:**

```bash
# Stop the application (Ctrl+C if running)
# Clean corrupted state stores
rm -rf /var/folders/*/kafka-streams* /tmp/kafka-streams*
# Restart the application
```

**Run the app:**

```bash
cd kafka-streams-demo
./gradlew run
```

> **Note**: If you encounter RocksDB errors about "wal_dir contains existing log file" or state store corruption, stop the application and run:
> ```bash
> rm -rf /var/folders/*/kafka-streams* /tmp/kafka-streams*
> ```
> Then restart the application. This cleans corrupted state stores from unclean shutdowns.

**Test it:**

```bash
# Send a message
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "hello world hello kafka", "userId": "user123"}'

# Check word counts (should return {"word":"hello","count":2})
curl http://localhost:8082/api/wordcounts/hello
curl http://localhost:8082/api/wordcounts/world
curl http://localhost:8082/api/wordcounts/kafka

# Send another message to see counts increment
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "hello beautiful world"}'

# Check updated count (should return {"word":"hello","count":3})
curl http://localhost:8082/api/wordcounts/hello
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
- **Working implementation with direct Kafka producer** (resolves common serialization issues)

## Current Status

✅ **Fully Working**: The application has been tested and verified to work correctly with:

- Message ingestion via REST API
- Real-time stream processing and word counting
- Persistent state management with RocksDB
- Query API for retrieving word counts
- Proper JSON serialization handling

## Technology Stack

- Micronaut 4.x for the web framework
- Kafka Streams for real-time processing
- Apache Kafka for messaging
- Jackson JSR310 for JSON serialization with Java 8 time support
- Java 17
- Gradle for builds

## Testing

Check out `TESTING.md` for detailed testing instructions including API examples, load testing, and automated test scripts.

## Troubleshooting

### Common Issues

**RocksDB State Store Corruption:**
If you see errors like "wal_dir contains existing log file" or "Error opening store word-count-store":

```bash
# Stop application (Ctrl+C)
rm -rf /var/folders/*/kafka-streams* /tmp/kafka-streams*
# Restart application
```

**Topics Not Found:**
Ensure Kafka topics are created before starting the application (see setup steps above).

**Message Producer Issues:**
If messages aren't being processed by Kafka Streams, this is often due to serialization issues. Our implementation uses a direct `KafkaProducer<String, String>` rather than Micronaut's `@KafkaClient` interface to ensure reliable JSON string serialization.

**Note**: This only replaces the producer component. Micronaut still handles:
- All REST API endpoints (`@Controller`, `@Get`, `@Post`)
- Dependency injection (`@Singleton` beans)
- Kafka Streams integration (`@KafkaStreamsFactory`)
- Configuration management and health checks

**Port Already in Use:**
If port 8082 is busy, kill the process using: `lsof -ti:8082 | xargs kill -9`

For detailed troubleshooting, see `TROUBLESHOOTING.md`.

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

## Author

Built by Mahitha Adapa
