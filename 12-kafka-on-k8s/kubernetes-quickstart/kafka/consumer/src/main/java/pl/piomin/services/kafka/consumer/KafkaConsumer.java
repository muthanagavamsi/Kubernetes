package pl.piomin.services.kafka.consumer;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import pl.piomin.services.kafka.common.Info;

@SpringBootApplication
@EnableKafka
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumer.class, args);
    }

    @Value("${app.in.topic}")
    private String topic;

    @Bean
    public NewTopic infoTopic() {
        return TopicBuilder.name(topic)
                .partitions(10)
                .replicas(3)
                .build();
    }

    @KafkaListener(id = "info", topics = "${app.in.topic}")
    public void onMessage(@Payload Info info,
                          @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) Long key,
                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        LOG.info("Received(key={}, partition={}): {}", key, partition, info);
    }
}
