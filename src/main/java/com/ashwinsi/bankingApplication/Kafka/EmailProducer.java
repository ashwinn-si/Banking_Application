package com.ashwinsi.bankingApplication.Kafka;

import com.ashwinsi.bankingApplication.DTO.EmailEventDTO;
import com.ashwinsi.bankingApplication.Utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j // This annotation automatically creates a logger object for your class.
@RequiredArgsConstructor // This annotation generates a constructor for all final fields (and @NonNull fields) in your class.
public class EmailProducer {
    private final KafkaTemplate<String, EmailEventDTO> kafkaTemplate;

    public void sendOtpEmail(EmailEventDTO event){
        kafkaTemplate.send(
                Constants.KAFKA_EMAIL_TOPIC,
                event.getToEmail(), // IT IS A KEY | Kafka uses the key to decide which partition the message goes to. | All messages with the same key (toEmail) go to the same partition
                event
        ).whenComplete((result, ex) -> {
            if(ex == null){
                log.info("OTP event sent for {} | partition={} offset={}",
                        event.getToEmail(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }else{
                log.error("Failed to send OTP event for {}: {}",
                        event.getToEmail(), ex.getMessage());
            }
        });
    }
}
