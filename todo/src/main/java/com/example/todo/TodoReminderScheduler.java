package com.example.todo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TodoReminderScheduler {

  private final TodoRepository todoRepository;
  private final MailService mailService;

  public TodoReminderScheduler(TodoRepository todoRepository, MailService mailService) {
    this.todoRepository = todoRepository;
    this.mailService = mailService;
  }

  @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Tokyo")
  public void sendDueSoonReminders() {
    LocalDate start = LocalDate.now();
    LocalDate end = start.plusDays(3);
    List<Todo> todos = todoRepository.findAllByDueDateBetweenAndCompletedFalse(start, end);
    Map<Long, List<Todo>> byUser = todos.stream()
        .filter(todo -> todo.getUser() != null && todo.getUser().getId() != null)
        .collect(Collectors.groupingBy(todo -> todo.getUser().getId()));
    for (List<Todo> userTodos : byUser.values()) {
      AppUser user = userTodos.get(0).getUser();
      mailService.sendDueSoonReminder(user, userTodos);
    }
  }
}
