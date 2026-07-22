package com.example.springai.controller;

import com.example.springai.model.ActorsFilms;
import com.example.springai.service.BeanOutputService;
import com.example.springai.service.ListOutputService;
import com.example.springai.service.MapOutputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class OutputConverterController {
    
    private final BeanOutputService beanOutputService;
    private final MapOutputService mapOutputService;
    private final ListOutputService listOutputService;
    
    /**
     * BeanOutputConverter - 단일 Bean
     * POST /chat/bean
     */
    @PostMapping("/bean")
    public ResponseEntity<ActorsFilms> getSingleActorFilms(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("message", "Tom Hanks");
        ActorsFilms actorsFilms = beanOutputService.getSingleActorFilms(prompt);

        log.info("Received ActorsFilms: {}", actorsFilms.toString());
        
        return ResponseEntity.ok(actorsFilms);
    }
    
    /**
     * BeanOutputConverter - Generic Bean Type (List<Bean>)
     * POST /chat/list-bean
     */
    @PostMapping("/list-bean")
    public ResponseEntity<List<ActorsFilms>> getMultipleActorsFilms(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("message", "Tom Hanks and Bill Murray");
        List<ActorsFilms> actorsFilmsList = beanOutputService.getMultipleActorsFilms(prompt);

        log.info("List of ActorsFilms: {}", actorsFilmsList.toString());
        
        return ResponseEntity.ok(actorsFilmsList);
    }
    
    /**
     * MapOutputConverter
     * POST /chat/map
     */
    @PostMapping("/map")
    public ResponseEntity<Map<String, Object>> getMapResult(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("message", "an array of numbers from 1 to 9 under they key name 'numbers'");
        Map<String, Object> result = mapOutputService.getMapResult(prompt);
    
        log.info("Map result: {}", result.toString());
        return ResponseEntity.ok(result);
    }
    
    /**
     * ListOutputConverter
     * POST /chat/list
     */
    @PostMapping("/list")
    public ResponseEntity<List<String>> getListResult(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("message", "ice cream flavors");
        List<String> flavors = listOutputService.getListResult(prompt);
        
        log.info("List result: {}", flavors.toString());
        return ResponseEntity.ok(flavors);
    }
}
