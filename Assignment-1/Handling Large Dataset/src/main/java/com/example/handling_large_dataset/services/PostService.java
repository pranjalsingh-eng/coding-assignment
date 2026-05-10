package com.example.handling_large_dataset.services;

import com.example.handling_large_dataset.entity.Post;
import com.example.handling_large_dataset.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Cacheable(value = "posts", key = "#page + '-' + #size")
    public Page<Post> getPostsUploaded(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "postDt"));
        return postRepository.findAll(pageable);
    }
}