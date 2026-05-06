
package com.example.handling_large_dataset.repository;

import com.example.handling_large_dataset.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;          // ✅ correct import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
}
