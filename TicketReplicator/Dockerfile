FROM confluentinc/cp-kafka-connect:latest

# Copy the Kafka Connect properties
COPY src/main/resources/connect-distributed.properties /etc/kafka/connect-distributed.properties

# Copy the JAR file
COPY build/libs/TicketReplicator-1.0-SNAPSHOT-all.jar /usr/share/java/kafka-connect-plugins/

# Manually copy the ClickHouse Kafka Connect plugin if available locally
# For example, if you downloaded the plugin and placed it in the local `libs` directory:
# COPY libs/clickhouse-kafka-connect-0.4-all.tar.gz /tmp/clickhouse-kafka-connect.tar.gz
# RUN mkdir -p /usr/share/confluent-hub-components && tar -xzf /tmp/clickhouse-kafka-connect.tar.gz -C /usr/share/confluent-hub-components

# Set environment variables
ENV CONNECT_BOOTSTRAP_SERVERS=your-kafka-bootstrap-server:9092
ENV CONNECT_GROUP_ID="connect-cluster"
ENV CONNECT_CONFIG_STORAGE_TOPIC="connect-configs"
ENV CONNECT_OFFSET_STORAGE_TOPIC="connect-offsets"
ENV CONNECT_STATUS_STORAGE_TOPIC="connect-status"
ENV CONNECT_KEY_CONVERTER="org.apache.kafka.connect.storage.StringConverter"
ENV CONNECT_VALUE_CONVERTER="org.apache.kafka.connect.json.JsonConverter"
ENV CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE="false"
ENV CONNECT_REST_ADVERTISED_HOST_NAME="localhost"
ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

# Expose the Kafka Connect REST API port
EXPOSE 8083

# Command to start Kafka Connect in distributed mode
CMD ["connect-distributed", "/etc/kafka/connect-distributed.properties"]
