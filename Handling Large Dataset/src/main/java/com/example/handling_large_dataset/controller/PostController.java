package com.example.handling_large_dataset.controller;

import com.example.handling_large_dataset.entity.Post;
import com.example.handling_large_dataset.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/getPostsUploaded")
    public ResponseEntity<Map<String, Object>> getPostsUploaded(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Post> postPage = postService.getPostsUploaded(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("posts",       postPage.getContent());
        response.put("currentPage", postPage.getNumber());
        response.put("totalItems",  postPage.getTotalElements());
        response.put("totalPages",  postPage.getTotalPages());

        return ResponseEntity.ok(response);
    }
}