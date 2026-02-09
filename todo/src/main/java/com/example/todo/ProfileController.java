package com.example.todo;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class ProfileController {

  private final AppUserRepository appUserRepository;
  private final TodoRepository todoRepository;

  public ProfileController(AppUserRepository appUserRepository, TodoRepository todoRepository) {
    this.appUserRepository = appUserRepository;
    this.todoRepository = todoRepository;
  }

  @GetMapping("/profile")
  public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    AppUser user = resolveUser(userDetails);
    String name = user != null ? user.getUsername() : "Guest";
    String title = user != null ? normalizeRoles(user.getRoles()) : "Viewer";
    String status = user != null && Boolean.TRUE.equals(user.getEnabled()) ? "有効" : "無効";
    String email = user != null ? user.getEmail() : "-";
    String roles = user != null ? user.getRoles() : "-";

    List<Todo> todos = user != null
        ? todoRepository.findAllByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId())
        : List.of();
    List<Todo> openTodos = todos.stream()
        .filter(todo -> todo.getStatus() == null || todo.getStatus() != TodoStatus.COMPLETED)
        .toList();
    long todoCount = todos.size();
    List<String> groupNames = todos.stream()
        .flatMap(todo -> todo.getGroups() == null ? java.util.stream.Stream.empty() : todo.getGroups().stream())
        .filter(g -> g != null && g.getName() != null)
        .map(Group::getName)
        .distinct()
        .sorted()
        .toList();

    model.addAttribute("userName", name);
    model.addAttribute("userTitle", title);
    model.addAttribute("userStatus", status);
    model.addAttribute("userEmail", email);
    model.addAttribute("userRoles", roles);
    model.addAttribute("userGroups", groupNames);
    model.addAttribute("openTodos", openTodos);

    model.addAttribute("stats", List.of(
        Map.of("label", "フォロワー", "value", "0"),
        Map.of("label", "フォロー中", "value", "0"),
        Map.of("label", "投稿数", "value", String.valueOf(todoCount))
    ));

    model.addAttribute("activities", List.of(
        "クライアントXの定例資料を更新",
        "プロジェクトApolloの進捗を共有",
        "部署横断タスクの優先度を整理"
    ));

    model.addAttribute("timeline", List.of(
        Map.of("time", "09:10", "title", "朝会の議事録を作成", "detail", "担当者へ共有済み"),
        Map.of("time", "11:45", "title", "クライアントYとの打合せ", "detail", "次回アクションを設定"),
        Map.of("time", "15:20", "title", "今週のタスク整理", "detail", "未完了のタスクを再配置")
    ));

    return "profile";
  }

  private AppUser resolveUser(UserDetails userDetails) {
    if (userDetails == null) {
      return null;
    }
    return appUserRepository.findByUsername(userDetails.getUsername()).orElse(null);
  }

  private String normalizeRoles(String roles) {
    if (roles == null || roles.isBlank()) {
      return "Member";
    }
    if (roles.contains("ROLE_ADMIN")) {
      return "管理者";
    }
    if (roles.contains("ROLE_USER")) {
      return "ユーザー";
    }
    return roles.replace("ROLE_", "");
  }
}
