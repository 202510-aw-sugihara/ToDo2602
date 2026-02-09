package com.example.todo;

public class DuplicateSubmissionException extends RuntimeException {
  public DuplicateSubmissionException(String message) {
    super(message);
  }
}
