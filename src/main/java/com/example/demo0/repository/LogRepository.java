package com.example.demo0.repository;
import com.example.demo0.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByUserId(Long userId);
    List<Log> findByAction(String action);
    List<Log> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}