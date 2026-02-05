package com.example.todo;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TodoNotFoundException.class)
  public String handleTodoNotFound(TodoNotFoundException ex, Model model) {
    model.addAttribute("message", ex.getMessage());
    return "error/404";
  }

  @ExceptionHandler(BusinessException.class)
  public String handleBusiness(BusinessException ex, Model model) {
    model.addAttribute("message", ex.getMessage());
    return "error/500";
  }

  @ExceptionHandler(Exception.class)
  public String handleGeneric(Exception ex, Model model) {
    model.addAttribute("message", "予期しないエラーが発生しました。");
    return "error/500";
  }
}
