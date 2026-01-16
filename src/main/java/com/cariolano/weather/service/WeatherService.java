package com.cariolano.weather.service;

import com.cariolano.weather.dto.CurrentWeatherDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrentWeatherDto getWeatherData(String city) {
        try {
            String normalizedCity = city.replace(".", " ").trim();
            if (normalizedCity.isBlank()) {
                throw new IllegalArgumentException("City name cannot be empty");
            }
            String encodedCity = URLEncoder.encode(normalizedCity, StandardCharsets.UTF_8);

            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity +
                    "&count=5&language=en&format=json";

            String geoResponse = restTemplate.getForObject(geoUrl, String.class);
            JsonNode geoJson = objectMapper.readTree(geoResponse);
            JsonNode results = geoJson.path("results");

            if (results.isMissingNode() || results.isEmpty()) {
                throw new RuntimeException("City not found: " + city);
            }

            JsonNode best = results.get(0);
            double latitude = best.path("latitude").asDouble();
            double longitude = best.path("longitude").asDouble();

            String weatherUrl = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=" + latitude +
                    "&longitude=" + longitude +
                    "&timezone=auto" +
                    "&current=temperature_2m,apparent_temperature,weather_code,wind_speed_10m,relative_humidity_2m" +
                    "&daily=temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min," +
                    "weather_code,precipitation_probability_max,precipitation_sum" +
                    "&temperature_unit=fahrenheit" +
                    "&wind_speed_unit=mph" +
                    "&precipitation_unit=inch";

            String weatherJson = restTemplate.getForObject(weatherUrl, String.class);
            JsonNode root = objectMapper.readTree(weatherJson);
            JsonNode current = root.path("current");
            JsonNode daily = root.path("daily");

            // Current weather
            CurrentWeatherDto dto = new CurrentWeatherDto(
                    current.path("time").asText(),
                    current.path("temperature_2m").asDouble(),
                    current.path("apparent_temperature").asDouble(),
                    current.path("weather_code").asInt(),
                    current.path("wind_speed_10m").asDouble(),
                    current.path("relative_humidity_2m").asInt(),
                    latitude,
                    longitude,
                    List.of()
            );

            // Daily forecast
            List<CurrentWeatherDto.Daily> forecast = new ArrayList<>();
            JsonNode timeArray = daily.path("time");

            if (!timeArray.isMissingNode() && timeArray.isArray()) {
                JsonNode tMax = daily.path("temperature_2m_max");
                JsonNode tMin = daily.path("temperature_2m_min");
                JsonNode appMax = daily.path("apparent_temperature_max");
                JsonNode appMin = daily.path("apparent_temperature_min");
                JsonNode wCode = daily.path("weather_code");
                JsonNode precipProb = daily.path("precipitation_probability_max");
                JsonNode precipSum = daily.path("precipitation_sum");

                for (int i = 0; i < timeArray.size(); i++) {
                    forecast.add(new CurrentWeatherDto.Daily(
                            timeArray.get(i).asText(),
                            tMax.get(i).asDouble(),
                            tMin.get(i).asDouble(),
                            appMax.get(i).asDouble(),
                            appMin.get(i).asDouble(),
                            wCode.get(i).asInt(),
                            precipProb.get(i).asDouble(),
                            precipSum.get(i).asDouble()
                    ));
                }
            }

            return new CurrentWeatherDto(
                    dto.time(),
                    dto.temperature_2m(),
                    dto.apparent_temperature(),
                    dto.weather_code(),
                    dto.wind_speed_10m(),
                    dto.relative_humidity_2m(),
                    dto.latitude(),
                    dto.longitude(),
                    forecast
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data for '" + city + "': " + e.getMessage(), e);
        }
    }
}