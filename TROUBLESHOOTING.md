# Troubleshooting Guide

Common issues and solutions when working with Micronaut + Kafka Streams applications.

## Application Startup Issues

### State Store Corruption

**Symptoms:**
- Application fails to start with state store errors
- `InvalidStateStoreException` during startup
- Corrupted state directory messages

**Causes:**
- Unclean application shutdown
- Disk space issues
- File system corruption

**Solutions:**

1. **Clean State Directory:**
   ```bash
   # Stop the application
   # Remove state directory (configured in application.yml)
   rm -rf /tmp/kafka-streams/word-count-app
   # Restart application
   ```

2. **Configure Cleanup Policy:**
   ```yaml
   kafka:
     streams:
       state:
         cleanup:
           delay:
             ms: 600000  # 10 minutes
   ```

3. **Implement Graceful Shutdown:**
   ```java
   @PreDestroy
   public void cleanup() {
       if (kafkaStreams != null) {
           kafkaStreams.close(Duration.ofSeconds(10));
       }
   }
   ```

### Kafka Connection Issues

**Symptoms:**
- `TimeoutException` during startup
- "Cannot connect to Kafka" errors
- Application hangs during initialization

**Solutions:**

1. **Verify Kafka is Running:**
   ```bash
   docker compose ps
   # Should show kafka and zookeeper as "Up"
   ```

2. **Check Network Connectivity:**
   ```bash
   telnet localhost 9092
   # Should connect successfully
   ```

3. **Verify Bootstrap Servers Configuration:**
   ```yaml
   kafka:
     bootstrap:
       servers: localhost:9092  # Ensure this matches your setup
   ```

## Runtime Issues

### High Memory Usage

**Symptoms:**
- OutOfMemoryError exceptions
- Gradual memory increase over time
- Poor application performance

**Solutions:**

1. **Configure State Store Cache Size:**
   ```yaml
   kafka:
     streams:
       cache:
         max:
           bytes:
             buffering: 10485760  # 10MB
   ```

2. **Use Windowed Operations:**
   ```java
   // Instead of unbounded aggregation
   .groupByKey()
   .windowedBy(TimeWindows.of(Duration.ofHours(24)))
   .count()
   ```

3. **Configure JVM Heap:**
   ```bash
   export JAVA_OPTS="-Xmx512m -Xms256m"
   ./gradlew run
   ```

### Processing Lag

**Symptoms:**
- Slow message processing
- Increasing consumer lag
- Delayed results in queries

**Solutions:**

1. **Increase Parallelism:**
   ```bash
   # Create topics with more partitions
   kafka-topics --create --topic text-messages \
     --bootstrap-server localhost:9092 \
     --partitions 10 \
     --replication-factor 1
   ```

2. **Scale Application Instances:**
   ```bash
   # Run multiple instances
   java -jar app.jar --micronaut.server.port=8081 &
   java -jar app.jar --micronaut.server.port=8082 &
   ```

3. **Optimize Processing Logic:**
   ```java
   // Avoid expensive operations in stream processing
   .filter(message -> !message.getContent().isEmpty())
   .mapValues(this::processEfficiently)
   ```

## Development Issues

### Docker Compose Problems

**Symptoms:**
- Containers fail to start
- Network connectivity issues
- Port conflicts

**Solutions:**

1. **Check Port Availability:**
   ```bash
   netstat -tlnp | grep -E '(2181|9092)'
   # Should show no conflicts
   ```

2. **Clean Docker State:**
   ```bash
   docker compose down -v
   docker system prune -f
   docker compose up -d
   ```

3. **Verify Container Logs:**
   ```bash
   docker compose logs kafka
   docker compose logs zookeeper
   ```

### Gradle Build Issues

**Symptoms:**
- Build failures
- Dependency resolution errors
- Compilation errors

**Solutions:**

1. **Clean Build:**
   ```bash
   ./gradlew clean build
   ```

2. **Check Java Version:**
   ```bash
   java -version
   # Should be Java 17 or higher
   ```

3. **Refresh Dependencies:**
   ```bash
   ./gradlew build --refresh-dependencies
   ```

## Testing Issues

### API Test Failures

**Symptoms:**
- HTTP 500 errors
- Connection refused errors
- Timeout exceptions

**Solutions:**

1. **Verify Application is Running:**
   ```bash
   curl http://localhost:8081/health
   # Should return 200 OK
   ```

2. **Check Application Logs:**
   ```bash
   tail -f logs/application.log
   ```

3. **Validate Request Format:**
   ```bash
   # Ensure proper JSON format
   curl -X POST http://localhost:8081/api/messages \
     -H "Content-Type: application/json" \
     -d '{"content": "test message", "userId": "test"}'
   ```

### State Store Query Issues

**Symptoms:**
- Empty query results
- `InvalidStateStoreException`
- Stale data in queries

**Solutions:**

1. **Wait for State Store Initialization:**
   ```java
   // Check if state store is ready
   ReadOnlyKeyValueStore<String, Long> store = 
       kafkaStreams.store(StoreQueryParameters.fromNameAndType(
           WORD_COUNT_STORE, QueryableStoreTypes.keyValueStore()));
   ```

2. **Verify Stream Processing:**
   ```bash
   # Check if messages are being processed
   kafka-console-consumer --bootstrap-server localhost:9092 \
     --topic word-counts --from-beginning
   ```

## Production Issues

### Performance Monitoring

**Key Metrics to Monitor:**
- Consumer lag
- Processing rate
- State store size
- Memory usage
- CPU utilization

**Monitoring Setup:**
```yaml
# Add to application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

### Deployment Issues

**Common Problems:**
- Resource constraints
- Network policies
- Configuration mismatches

**Best Practices:**
- Use health checks in orchestration
- Configure proper resource limits
- Implement graceful shutdown
- Monitor application metrics

## Getting Help

If you encounter issues not covered in this guide:

1. **Check Application Logs:** Look for specific error messages
2. **Verify Configuration:** Ensure all settings match your environment
3. **Test Components Individually:** Isolate the problem area
4. **Consult Documentation:** Refer to Micronaut and Kafka Streams docs
5. **Community Support:** Ask questions on relevant forums

## Useful Commands

### Kafka Operations
```bash
# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
kafka-topics --describe --topic text-messages --bootstrap-server localhost:9092

# Reset consumer group
kafka-streams-application-reset --application-id word-count-app \
  --bootstrap-servers localhost:9092
```

### Application Debugging
```bash
# Enable debug logging
export MICRONAUT_ENVIRONMENTS=development
./gradlew run

# Check JVM metrics
jstat -gc [PID]

# Monitor file descriptors
lsof -p [PID] | wc -l
```

This troubleshooting guide covers the most common issues encountered when developing and deploying Micronaut + Kafka Streams applications. Keep it handy for quick reference during development and production operations.

