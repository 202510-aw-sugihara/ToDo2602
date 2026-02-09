package com.example.todo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  List<Todo> findByTitleContainingIgnoreCaseAndDeletedAtIsNull(String keyword);

  List<Todo> findByDueDateLessThanEqualAndDeletedAtIsNull(LocalDate date);

  List<Todo> findAllByDeletedAtIsNullOrderByPriorityAsc();

  List<Todo> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

  List<Todo> findAllByDueDateBetweenAndStatusNotAndDeletedAtIsNull(LocalDate start, LocalDate end, TodoStatus status);

  @Query("select t from Todo t where t.deletedAt is null and t.status = :status and t.title like %:keyword%")
  List<Todo> searchByStatusAndTitle(@Param("status") TodoStatus status,
      @Param("keyword") String keyword);

  @Query("select t from Todo t where t.deletedAt is null and t.title like %:keyword%")
  List<Todo> searchByTitle(@Param("keyword") String keyword);

  List<Todo> findAllByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

  Optional<Todo> findByIdAndDeletedAtIsNull(Long id);

  List<Todo> findAllByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
