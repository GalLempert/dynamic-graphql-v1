package com.example.dynamicgraphql.web;

import com.example.dynamicgraphql.service.DocumentMutationService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentMutationService mutationService;

    public DocumentController(DocumentMutationService mutationService) {
        this.mutationService = mutationService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Map<String, Object> document) {
        mutationService.persist(document);
        return ResponseEntity.accepted().build();
    }

    @PutMapping
    public ResponseEntity<Void> update(@RequestBody Map<String, Object> document) {
        mutationService.update(document);
        return ResponseEntity.accepted().build();
    }
}
