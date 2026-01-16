package com.cariolano.weather.controller;

import com.cariolano.weather.dto.CurrentWeatherDto;
import com.cariolano.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public CurrentWeatherDto getWeather(@RequestParam String city) {
        return weatherService.getWeatherData(city);
    }
}