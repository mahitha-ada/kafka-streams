# Testing Guide

Comprehensive testing instructions for the Micronaut + Kafka Streams application.

## Prerequisites

Before running tests, ensure you have:

1. **Kafka Infrastructure Running:**

   ```bash
   docker compose up -d
   ```

2. **Application Started:**

   ```bash
   cd kafka-streams-demo
   ./gradlew run
   ```

3. **Verify Health:**
   ```bash
   curl http://localhost:8082/health
   # Expected: {"status":"UP"}
   ```

## API Testing

### Message Ingestion

**Send a Simple Message:**

```bash
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "hello world hello kafka streams",
    "userId": "user123"
  }'
```

**Expected Response:**

```
Message sent with ID: [UUID]
```

**Send Multiple Messages:**

```bash
# Message 1
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "kafka streams processing", "userId": "user1"}'

# Message 2
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "real time data processing", "userId": "user2"}'

# Message 3
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "micronaut kafka integration", "userId": "user3"}'
```

### Word Count Queries

**Query Specific Word:**

```bash
curl http://localhost:8082/api/wordcounts/processing
```

**Expected Response:**

```json
{
  "word": "processing",
  "count": 2
}
```

**Query Non-existent Word:**

```bash
curl http://localhost:8082/api/wordcounts/nonexistent
```

**Expected Response:**

```json
{
  "word": "nonexistent",
  "count": 0
}
```

**Get Top Words:**

```bash
curl http://localhost:8082/api/wordcounts?limit=5
```

**Expected Response:**

```json
[
  { "word": "processing", "count": 2 },
  { "word": "data", "count": 1 },
  { "word": "kafka", "count": 2 },
  { "word": "streams", "count": 1 },
  { "word": "real", "count": 1 }
]
```

**Get All Words (Default Limit):**

```bash
curl http://localhost:8082/api/wordcounts
```

## Integration Testing

### End-to-End Workflow

**Complete Test Scenario:**

```bash
#!/bin/bash

echo "=== Starting End-to-End Test ==="

# 1. Send test message
echo "Sending test message..."
RESPONSE=$(curl -s -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "testing one two testing", "userId": "test-user"}')
echo "Response: $RESPONSE"

# 2. Wait for processing (Kafka Streams needs time)
echo "Waiting for stream processing..."
sleep 3

# 3. Query word count
echo "Querying word count for 'testing'..."
COUNT_RESPONSE=$(curl -s http://localhost:8082/api/wordcounts/testing)
echo "Count Response: $COUNT_RESPONSE"

# 4. Verify result
if echo "$COUNT_RESPONSE" | grep -q '"count":2'; then
    echo "✅ Test PASSED: Word count is correct"
else
    echo "❌ Test FAILED: Expected count 2 for 'testing'"
fi

echo "=== Test Complete ==="
```

### Load Testing

**Send Multiple Messages Rapidly:**

```bash
#!/bin/bash

echo "=== Load Test: Sending 100 messages ==="

for i in {1..100}; do
    curl -s -X POST http://localhost:8082/api/messages \
      -H "Content-Type: application/json" \
      -d "{\"content\": \"load test message number $i\", \"userId\": \"load-test-$i\"}" &

    # Limit concurrent requests
    if (( i % 10 == 0 )); then
        wait
        echo "Sent $i messages..."
    fi
done

wait
echo "✅ Load test complete: 100 messages sent"

# Wait for processing
sleep 5

# Check results
echo "Checking word counts..."
curl -s http://localhost:8082/api/wordcounts/load | jq .
curl -s http://localhost:8082/api/wordcounts/test | jq .
curl -s http://localhost:8082/api/wordcounts/message | jq .
```

## Unit Testing

### Running Application Tests

**Execute All Tests:**

```bash
cd kafka-streams-demo
./gradlew test
```

**Run Specific Test Class:**

```bash
./gradlew test --tests "com.example.KafkaStreamsDemoTest"
```

**Run Tests with Coverage:**

```bash
./gradlew test jacocoTestReport
# View coverage report at: build/reports/jacoco/test/html/index.html
```

### Test Categories

**Controller Tests:**

- Message ingestion endpoint validation
- Word count query endpoint validation
- Error handling scenarios

**Service Tests:**

- Kafka producer functionality
- State store query operations
- Business logic validation

**Integration Tests:**

- End-to-end message flow
- Kafka Streams topology testing
- State store persistence

## Performance Testing

### Throughput Testing

**Measure Message Processing Rate:**

```bash
#!/bin/bash

START_TIME=$(date +%s)
MESSAGE_COUNT=1000

echo "=== Throughput Test: $MESSAGE_COUNT messages ==="

for i in $(seq 1 $MESSAGE_COUNT); do
    curl -s -X POST http://localhost:8082/api/messages \
      -H "Content-Type: application/json" \
      -d "{\"content\": \"throughput test $i\", \"userId\": \"perf-test\"}" > /dev/null &

    # Batch requests to avoid overwhelming the system
    if (( i % 50 == 0 )); then
        wait
        echo "Sent $i/$MESSAGE_COUNT messages..."
    fi
done

wait

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
RATE=$((MESSAGE_COUNT / DURATION))

echo "✅ Throughput test complete:"
echo "   Messages: $MESSAGE_COUNT"
echo "   Duration: ${DURATION}s"
echo "   Rate: ${RATE} messages/second"
```

### Latency Testing

**Measure Response Times:**

```bash
#!/bin/bash

echo "=== Latency Test ==="

for i in {1..10}; do
    START=$(date +%s%N)

    curl -s -X POST http://localhost:8082/api/messages \
      -H "Content-Type: application/json" \
      -d '{"content": "latency test", "userId": "latency-test"}' > /dev/null

    END=$(date +%s%N)
    LATENCY=$(( (END - START) / 1000000 ))  # Convert to milliseconds

    echo "Request $i: ${LATENCY}ms"
done
```

## Error Testing

### Invalid Request Testing

**Missing Required Fields:**

```bash
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"userId": "test"}'
# Expected: 400 Bad Request
```

**Invalid JSON:**

```bash
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "test", "userId": }'
# Expected: 400 Bad Request
```

**Empty Content:**

```bash
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "", "userId": "test"}'
# Expected: Should process but not generate word counts
```

### Service Unavailability Testing

**Test with Kafka Down:**

```bash
# Stop Kafka
docker compose stop kafka

# Try sending message
curl -X POST http://localhost:8082/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "test", "userId": "test"}'
# Expected: 500 Internal Server Error

# Restart Kafka
docker compose start kafka
```

## Monitoring During Tests

### Application Metrics

**Health Check:**

```bash
curl http://localhost:8082/health
```

**Application Info:**

```bash
curl http://localhost:8082/info
```

### Kafka Monitoring

**Check Topic Messages:**

```bash
# View messages in input topic
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic text-messages --from-beginning --timeout-ms 5000

# View word counts in output topic
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic word-counts --from-beginning --timeout-ms 5000
```

**Check Consumer Lag:**

```bash
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group word-count-app
```

## Test Data Cleanup

### Reset Application State

**Clean State Stores:**

```bash
# Stop application
# Remove state directory
rm -rf /tmp/kafka-streams/word-count-app
# Restart application
```

**Reset Kafka Topics:**

```bash
# Delete and recreate topics
kafka-topics --delete --topic text-messages --bootstrap-server localhost:9092
kafka-topics --delete --topic word-counts --bootstrap-server localhost:9092

# Topics will be auto-created when application restarts
```

## Automated Testing Script

**Complete Test Suite:**

```bash
#!/bin/bash

echo "=== Micronaut Kafka Streams Test Suite ==="

# Function to check if service is ready
wait_for_service() {
    echo "Waiting for application to be ready..."
    for i in {1..30}; do
        if curl -s http://localhost:8082/health > /dev/null; then
            echo "✅ Application is ready"
            return 0
        fi
        sleep 2
    done
    echo "❌ Application failed to start"
    exit 1
}

# Function to run test and check result
run_test() {
    local test_name="$1"
    local command="$2"
    local expected="$3"

    echo "Running: $test_name"
    result=$(eval "$command")

    if echo "$result" | grep -q "$expected"; then
        echo "✅ PASS: $test_name"
        return 0
    else
        echo "❌ FAIL: $test_name"
        echo "   Expected: $expected"
        echo "   Got: $result"
        return 1
    fi
}

# Wait for application
wait_for_service

# Run tests
PASS_COUNT=0
TOTAL_COUNT=0

# Test 1: Health check
((TOTAL_COUNT++))
if run_test "Health Check" "curl -s http://localhost:8082/health" "UP"; then
    ((PASS_COUNT++))
fi

# Test 2: Send message
((TOTAL_COUNT++))
if run_test "Send Message" "curl -s -X POST http://localhost:8082/api/messages -H 'Content-Type: application/json' -d '{\"content\": \"test message\", \"userId\": \"test\"}'" "Message sent"; then
    ((PASS_COUNT++))
fi

# Wait for processing
sleep 3

# Test 3: Query word count
((TOTAL_COUNT++))
if run_test "Query Word Count" "curl -s http://localhost:8082/api/wordcounts/test" "test"; then
    ((PASS_COUNT++))
fi

# Test 4: Get top words
((TOTAL_COUNT++))
if run_test "Get Top Words" "curl -s http://localhost:8082/api/wordcounts?limit=5" "word"; then
    ((PASS_COUNT++))
fi

# Summary
echo "=== Test Results ==="
echo "Passed: $PASS_COUNT/$TOTAL_COUNT"

if [ $PASS_COUNT -eq $TOTAL_COUNT ]; then
    echo "🎉 All tests passed!"
    exit 0
else
    echo "💥 Some tests failed!"
    exit 1
fi
```

Save this as `run-tests.sh`, make it executable with `chmod +x run-tests.sh`, and run with `./run-tests.sh`.

## Troubleshooting Test Issues

If tests fail, check:

1. **Application Status:** `curl http://localhost:8082/health`
2. **Kafka Status:** `docker compose ps`
3. **Application Logs:** Check console output for errors
4. **Network Connectivity:** Ensure ports 8081, 9092, 2181 are accessible
5. **Timing Issues:** Add delays between sending messages and querying results

For more detailed troubleshooting, see `TROUBLESHOOTING.md`.
