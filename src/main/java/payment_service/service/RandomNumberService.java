package payment_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import payment_service.exception.ExternalApiException;

@Component
@RequiredArgsConstructor
public class RandomNumberService {
    private final WebClient webClient;

    public Boolean isEvenNumber() {
        Integer[] numbers = webClient.get()
                .uri("/generateNumbers?min=1&max=10&limit=1")
                .retrieve()
                .bodyToMono(Integer[].class)
                .block();
        if (numbers == null) {
            throw new ExternalApiException();
        }
        int number = numbers[0];
        return number % 2 == 0;
    }
}
