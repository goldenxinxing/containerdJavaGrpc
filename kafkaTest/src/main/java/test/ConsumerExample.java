/**
 * Copyright 2020 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerExample {

  public static void main(final String[] args) throws Exception {

    final String topic = "GALAXY_REALTIME_VIDEO_CAPTURE";

    // Load properties from a local configuration file
    // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with configuration parameters
    // to connect to your Kafka cluster, which can be on your local host, Confluent Cloud, or any other cluster.
    // Follow these instructions to create this file: https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html

    final Properties props = new Properties();

    // Add additional properties.
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "demo-consumer-1");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "MEGVII1010455916:23310");

    final Consumer<String, Object> consumer = new KafkaConsumer<String, Object>(props);
    consumer.subscribe(Arrays.asList(topic));

    Long total_count = 0L;

    try {
      while (true) {
        ConsumerRecords<String, Object> records = consumer.poll(100);
        for (ConsumerRecord<String, Object> record : records) {
          String key = record.key();
          Object value = record.value();
          System.out.printf("Consumed record with key %s and value %s, and updated total count to %d%n", key, value, total_count);
        }
      }
    } finally {
      consumer.close();
    }
  }


  public static Properties loadConfig(String configFile) throws IOException {
    if (!Files.exists(Paths.get(configFile))) {
      throw new IOException(configFile + " not found.");
    }
    final Properties cfg = new Properties();
    try (InputStream inputStream = new FileInputStream(configFile)) {
      cfg.load(inputStream);
    }
    return cfg;
  }

}
