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

  @Query("select distinct t from Todo t join t.groups g "
      + "where t.deletedAt is null and g.id in :groupIds and t.user.id <> :userId "
      + "order by t.createdAt desc")
  List<Todo> findSharedByGroupIds(@Param("userId") Long userId,
      @Param("groupIds") List<Long> groupIds);

  Optional<Todo> findByIdAndDeletedAtIsNull(Long id);

  List<Todo> findAllByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
