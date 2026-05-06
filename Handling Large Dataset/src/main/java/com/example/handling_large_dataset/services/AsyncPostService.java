package com.example.handling_large_dataset.services;

import com.example.handling_large_dataset.entity.Post;
import com.example.handling_large_dataset.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncPostService {

    private final PostRepository postRepository;
    @Async
    @Cacheable(value = "posts")
    public CompletableFuture<List<Post>> getPostsAsync(Pageable pageable) {
        List<Post> posts = postRepository.findAll(pageable).getContent();
        return CompletableFuture.completedFuture(posts);
        // Runs in a separate thread pool — does not block HTTP worker threads
    }
}
