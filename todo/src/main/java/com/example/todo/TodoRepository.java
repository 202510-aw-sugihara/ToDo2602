package com.example.todo;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  List<Todo> findByCompleted(boolean completed);

  List<Todo> findByTitleContainingIgnoreCase(String keyword);

  List<Todo> findByDueDateLessThanEqual(LocalDate date);

  List<Todo> findAllByOrderByPriorityAsc();

  List<Todo> findAllByOrderByCreatedAtDesc();

  @Query("select t from Todo t where t.completed = :completed and t.title like %:keyword%")
  List<Todo> searchByStatusAndTitle(@Param("completed") boolean completed,
      @Param("keyword") String keyword);

  @Query("select t from Todo t where t.title like %:keyword%")
  List<Todo> searchByTitle(@Param("keyword") String keyword);

  List<Todo> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}
