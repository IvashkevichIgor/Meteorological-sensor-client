package ru.ivashkevich.meteorologicalsensorclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.ivashkevich.meteorologicalsensorclient.models.SensorClient;

@SpringBootApplication
public class MeteorologicalSensorClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeteorologicalSensorClientApplication.class, args);
        SensorClient sensorClient = new SensorClient();

        String sensorName = "Ginger";

        sensorClient.registerSensor(sensorName);

        for (int i = 0; i < 1000; i++){
            sensorClient.postMeasurement(sensorName);
        }

        sensorClient.getAllMeasurements();
    }

}
