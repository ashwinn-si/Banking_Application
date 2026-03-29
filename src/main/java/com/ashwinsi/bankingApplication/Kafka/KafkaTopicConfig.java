package com.ashwinsi.bankingApplication.Kafka;

import com.ashwinsi.bankingApplication.Utils.Constants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaTopicConfig {
    public static final String OTP_TOPIC = Constants.KAFKA_EMAIL_TOPIC;

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(OTP_TOPIC).partitions(3) // 3 partitions for parallelism
                .replicas(1).build();
    }
}
