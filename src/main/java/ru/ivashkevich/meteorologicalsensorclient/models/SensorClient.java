package ru.ivashkevich.meteorologicalsensorclient.models;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.ivashkevich.meteorologicalsensorclient.dto.MeasurementDTO;
import ru.ivashkevich.meteorologicalsensorclient.dto.SensorDTO;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class SensorClient {

    private final WebClient webClient;

    public SensorClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public void registerSensor(String name){
        SensorDTO sensorDTO = new SensorDTO();
        sensorDTO.setName(name);

        webClient.post()
                .uri("/sensors/registration")
                .body(BodyInserters.fromValue(sensorDTO))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class);
                    } else  {
                        return Mono.just("Error response");
                    }
                })
                //.subscribe(System.out::println);
                .block();
    }

    public void postMeasurement(String sensorName){
        MeasurementDTO measurementDTO = getRandomMeasurementDTOForSensor(sensorName);
        webClient.post()
                .uri("/measurements/add")
                .body(BodyInserters.fromValue(measurementDTO))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class);
                    } else  {
                        return Mono.just("Error response");
                    }
                })
                //.subscribe(System.out::println);
                .block();
    }

    public void getAllMeasurements(){
        webClient.get()
                .uri("/measurements")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(MeasurementDTO.class)
                .subscribe(System.out::println);
    }

    private MeasurementDTO getRandomMeasurementDTOForSensor(String sensorName){
        SensorDTO sensorDTO = new SensorDTO();
        sensorDTO.setName(sensorName);
        MeasurementDTO measurementDTO = new MeasurementDTO();
        measurementDTO.setValue(Math.random() * 200 - 100);
        measurementDTO.setRaining(Math.random() < 0.5);
        measurementDTO.setSensor(sensorDTO);

        return  measurementDTO;
    }
}
