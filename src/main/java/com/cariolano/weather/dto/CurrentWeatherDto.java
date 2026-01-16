package com.cariolano.weather.dto;

import java.util.List;

public record CurrentWeatherDto(String time,
                                double temperature_2m,
                                double apparent_temperature,
                                int weather_code,
                                double wind_speed_10m,
                                int relative_humidity_2m,
                                double latitude,
                                double longitude,
                                List<Daily> dailyForecast) {
    public record Daily(String date,                        // e.g. "2026-01-17"
                        double temperatureMax,
                        double temperatureMin,
                        double apparentTemperatureMax,
                        double apparentTemperatureMin,
                        int weatherCode,
                        double precipitationProbabilityMax, // 0–100%
                        double precipitationSum){}
}
