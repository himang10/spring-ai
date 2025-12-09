package com.example.springai.controller;

import com.example.springai.model.ActorsFilms;
import com.example.springai.service.BeanOutputService;
import com.example.springai.service.ListOutputService;
import com.example.springai.service.MapOutputService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        String actor = request.getOrDefault("message", "Tom Hanks");
        ActorsFilms actorsFilms = beanOutputService.getSingleActorFilms(actor);
        
        return ResponseEntity.ok(actorsFilms);
    }
    
    /**
     * BeanOutputConverter - Generic Bean Type (List<Bean>)
     * POST /chat/list-bean
     */
    @PostMapping("/list-bean")
    public ResponseEntity<List<ActorsFilms>> getMultipleActorsFilms(@RequestBody Map<String, String> request) {
        String actors = request.getOrDefault("message", "Tom Hanks and Bill Murray");
        List<ActorsFilms> actorsFilmsList = beanOutputService.getMultipleActorsFilms(actors);
        
        return ResponseEntity.ok(actorsFilmsList);
    }
    
    /**
     * MapOutputConverter
     * POST /chat/map
     */
    @PostMapping("/map")
    public ResponseEntity<Map<String, Object>> getMapResult(@RequestBody Map<String, String> request) {
        String subject = request.getOrDefault("message", "an array of numbers from 1 to 9 under they key name 'numbers'");
        Map<String, Object> result = mapOutputService.getMapResult(subject);
    
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ListOutputConverter
     * POST /chat/list
     */
    @PostMapping("/list")
    public ResponseEntity<List<String>> getListResult(@RequestBody Map<String, String> request) {
        String subject = request.getOrDefault("message", "ice cream flavors");
        List<String> flavors = listOutputService.getListResult(subject);
        
        return ResponseEntity.ok(flavors);
    }
}
