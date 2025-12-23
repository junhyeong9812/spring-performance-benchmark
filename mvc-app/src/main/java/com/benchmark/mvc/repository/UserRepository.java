package com.benchmark.mvc.repository;

import com.benchmark.mvc.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
  List<User> findAllOrderByCreatedAtDesc();
}
