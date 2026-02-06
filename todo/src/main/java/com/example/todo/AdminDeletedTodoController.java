package com.example.todo;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/deleted-todos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeletedTodoController {

  private final TodoService todoService;

  public AdminDeletedTodoController(TodoService todoService) {
    this.todoService = todoService;
  }

  @GetMapping
  public String list(Model model) {
    List<Todo> deleted = todoService.findDeleted();
    model.addAttribute("deletedTodos", deleted);
    return "admin/deleted_todos";
  }

  @PostMapping("/{id}/restore")
  public String restore(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
    try {
      todoService.restoreById(id);
      redirectAttributes.addFlashAttribute("successMessage", "削除済みToDoを復元しました。");
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", "対象のToDoが見つかりません。");
    }
    return "redirect:/admin/deleted-todos";
  }

  @PostMapping("/{id}/purge")
  public String purge(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
    try {
      todoService.deleteByIdHard(id);
      redirectAttributes.addFlashAttribute("successMessage", "削除済みToDoを完全削除しました。");
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", "対象のToDoが見つかりません。");
    }
    return "redirect:/admin/deleted-todos";
  }
}
