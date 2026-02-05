package com.example.todo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todo_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "todo_id", nullable = false)
  private Todo todo;

  @Column(nullable = false, length = 255)
  private String originalFilename;

  @Column(nullable = false, length = 255)
  private String storedFilename;

  @Column(length = 100)
  private String contentType;

  @Column(nullable = false)
  private long size;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;
}
