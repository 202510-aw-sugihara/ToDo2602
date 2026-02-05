package com.example.todo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/todos")
public class TodoController {

  private final TodoService todoService;
  private final CategoryRepository categoryRepository;
  private final AppUserRepository appUserRepository;

  public TodoController(TodoService todoService, CategoryRepository categoryRepository,
      AppUserRepository appUserRepository) {
    this.todoService = todoService;
    this.categoryRepository = categoryRepository;
    this.appUserRepository = appUserRepository;
  }

  @ModelAttribute("categories")
  public List<Category> categories() {
    return categoryRepository.findAll();
  }

  @GetMapping
  public String list(@RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) Long categoryId,
      @PageableDefault(size = 10) Pageable pageable,
      @AuthenticationPrincipal UserDetails userDetails,
      Model model) {

    long userId = requireUserId(userDetails);
    Page<Todo> page = todoService.findPage(userId, keyword, sort, direction, categoryId, pageable);
    model.addAttribute("todos", page.getContent());
    model.addAttribute("page", page);
    model.addAttribute("keyword", keyword == null ? "" : keyword);
    model.addAttribute("sort", sort == null ? "createdAt" : sort);
    model.addAttribute("direction", direction == null ? "desc" : direction);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("resultCount", page.getTotalElements());

    long total = page.getTotalElements();
    long start = total == 0 ? 0 : (page.getNumber() * (long) page.getSize()) + 1;
    long end = total == 0 ? 0 : Math.min(start + page.getSize() - 1, total);
    model.addAttribute("start", start);
    model.addAttribute("end", end);
    return "index";
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) List<Long> ids,
      @AuthenticationPrincipal UserDetails userDetails) {
    long userId = requireUserId(userDetails);
    List<Todo> todos = (ids != null && !ids.isEmpty())
        ? todoService.findForExportByIds(userId, ids)
        : todoService.findForExport(userId, keyword, sort, direction, categoryId);
    if (todos.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    StringBuilder csv = new StringBuilder();
    csv.append("ID,タイトル,登録者,ステータス,作成日\r\n");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    for (Todo todo : todos) {
      csv.append(csvCell(todo.getId() == null ? "" : String.valueOf(todo.getId()))).append(",");
      csv.append(csvCell(todo.getTitle())).append(",");
      csv.append(csvCell(todo.getAuthor())).append(",");
      csv.append(csvCell(todo.isCompleted() ? "完了" : "未完了")).append(",");
      String createdAt = todo.getCreatedAt() == null ? "" : todo.getCreatedAt().format(dateFormatter);
      csv.append(csvCell(createdAt)).append("\r\n");
    }

    byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
    byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    byte[] bytes = new byte[bom.length + body.length];
    System.arraycopy(bom, 0, bytes, 0, bom.length);
    System.arraycopy(body, 0, bytes, bom.length, body.length);

    String filename = "todo_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

    return ResponseEntity.ok().headers(headers).body(bytes);
  }

  private String csvCell(String value) {
    if (value == null) {
      return "";
    }
    boolean needsQuote = value.contains(",") || value.contains("\"")
        || value.contains("\r") || value.contains("\n");
    String escaped = value.replace("\"", "\"\"");
    return needsQuote ? "\"" + escaped + "\"" : escaped;
  }

  @GetMapping("/new")
  public String newTodo(@ModelAttribute("todoForm") TodoForm form) {
    if (form.getDueDate() == null) {
      form.setDueDate(java.time.LocalDate.now().plusWeeks(1));
    }
    return "todo/new";
  }

  @PostMapping("/confirm")
  public String confirm(@Valid @ModelAttribute("todoForm") TodoForm form, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "todo/new";
    }
    return "todo/confirm";
  }

  @PostMapping("/back")
  public String back(@ModelAttribute("todoForm") TodoForm form, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("todoForm", form);
    return "redirect:/todos/new";
  }

  @PostMapping("/complete")
  public String complete(@ModelAttribute("todoForm") TodoForm form,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {
    long userId = requireUserId(userDetails);
    todoService.create(userId, form);
    redirectAttributes.addFlashAttribute("successMessage", "登録が完了しました。");
    return "redirect:/todos";
  }

  @GetMapping("/{id:\\d+}")
  public String detail(@PathVariable("id") long id, Model model,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {
    Todo todo = todoService.findById(id).orElse(null);
    if (todo == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "指定されたToDoが見つかりませんでした。");
      return "redirect:/todos";
    }
    ensureOwner(todo, requireUserId(userDetails));
    model.addAttribute("todo", todo);
    return "todo/detail";
  }

  @GetMapping("/{id:\\d+}/edit")
  public String edit(@PathVariable("id") long id, Model model,
      @AuthenticationPrincipal UserDetails userDetails) {
    Todo todo = todoService.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    ensureOwner(todo, requireUserId(userDetails));
    model.addAttribute("todoForm", todoService.toForm(todo));
    return "todo/edit";
  }

  @PostMapping("/{id:\\d+}/update")
  public String update(@PathVariable("id") long id,
      @Valid @ModelAttribute("todoForm") TodoForm form,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {

    if (bindingResult.hasErrors()) {
      return "todo/edit";
    }

    try {
      Todo existing = todoService.findById(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      ensureOwner(existing, requireUserId(userDetails));
      todoService.update(id, form);
    } catch (OptimisticLockingFailureException ex) {
      model.addAttribute("errorMessage", "ほかのユーザーが更新しました。もう一度やり直してください。");
      return "todo/edit";
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    redirectAttributes.addFlashAttribute("successMessage", "更新が完了しました。");
    return "redirect:/todos";
  }

  @DeleteMapping("/{id:\\d+}")
  public String delete(@PathVariable("id") long id,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {
    try {
      Todo existing = todoService.findById(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      ensureOwner(existing, requireUserId(userDetails));
      todoService.deleteById(id);
      redirectAttributes.addFlashAttribute("successMessage", "ToDoを削除しました。");
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました。");
    }
    return "redirect:/todos";
  }

  @PostMapping("/bulk-delete")
  public String bulkDelete(@RequestParam(name = "ids", required = false) List<Long> ids,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {
    long userId = requireUserId(userDetails);
    int deleted = todoService.deleteByIds(userId, ids);
    if (deleted > 0) {
      redirectAttributes.addFlashAttribute("successMessage", "選択したToDoを削除しました。");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "削除対象が選択されていません。");
    }
    return "redirect:/todos";
  }

  @PostMapping("/{id:\\d+}/toggle")
  public Object toggle(@PathVariable("id") long id,
      HttpServletRequest request,
      RedirectAttributes redirectAttributes,
      @AuthenticationPrincipal UserDetails userDetails) {

    boolean ajax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

    try {
      Todo existing = todoService.findById(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      ensureOwner(existing, requireUserId(userDetails));
      boolean completed = todoService.toggleCompleted(id);
      if (ajax) {
        return ResponseEntity.ok(Map.of("completed", completed));
      }
      redirectAttributes.addFlashAttribute("successMessage", "完了状態を更新しました。");
      return "redirect:/todos";
    } catch (ResponseStatusException ex) {
      if (ajax) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", "forbidden"));
      }
      throw ex;
    } catch (IllegalArgumentException ex) {
      if (ajax) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not_found"));
      }
      redirectAttributes.addFlashAttribute("errorMessage", "対象のToDoが見つかりませんでした。");
      return "redirect:/todos";
    }
  }

  private long requireUserId(UserDetails userDetails) {
    if (userDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return appUserRepository.findByUsername(userDetails.getUsername())
        .map(AppUser::getId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  private void ensureOwner(Todo todo, long userId) {
    if (todo.getUser() == null || todo.getUser().getId() == null
        || todo.getUser().getId().longValue() != userId) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }
}
