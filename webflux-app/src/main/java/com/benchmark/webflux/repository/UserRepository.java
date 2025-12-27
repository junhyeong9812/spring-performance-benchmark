package com.benchmark.webflux.repository;

import com.benchmark.webflux.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

  @Query("SELECT * FROM users ORDER BY created_at DESC")
  Flux<User> findAllOrderByCreatedAtDesc();
}
