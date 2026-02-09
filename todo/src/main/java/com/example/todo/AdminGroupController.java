package com.example.todo;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroupController {

  private final GroupRepository groupRepository;
  private final MessageSource messageSource;

  public AdminGroupController(GroupRepository groupRepository, MessageSource messageSource) {
    this.groupRepository = groupRepository;
    this.messageSource = messageSource;
  }

  @GetMapping
  public String list(@ModelAttribute("groupForm") GroupForm form, Model model) {
    List<Group> groups = groupRepository.findAllByOrderByTypeAscNameAsc();
    List<Group> parentCandidates = groupRepository.findAllByOrderByTypeAscNameAsc();
    model.addAttribute("groups", groups);
    model.addAttribute("parentCandidates", parentCandidates);
    return "admin/groups";
  }

  @PostMapping
  public String create(@Valid @ModelAttribute("groupForm") GroupForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    Group parent = resolveParent(form);
    validateParent(form, parent, bindingResult);
    if (bindingResult.hasErrors()) {
      List<Group> groups = groupRepository.findAllByOrderByTypeAscNameAsc();
      List<Group> parentCandidates = groupRepository.findAllByOrderByTypeAscNameAsc();
      model.addAttribute("groups", groups);
      model.addAttribute("parentCandidates", parentCandidates);
      return "admin/groups";
    }
    Group group = Group.builder()
        .name(form.getName())
        .type(form.getType())
        .parent(parent)
        .color(form.getColor())
        .build();
    groupRepository.save(group);
    redirectAttributes.addFlashAttribute("successMessage", msg("msg.group_created"));
    return "redirect:/admin/groups";
  }

  private Group resolveParent(GroupForm form) {
    if (form.getParentId() == null) {
      return null;
    }
    return groupRepository.findById(form.getParentId()).orElse(null);
  }

  private void validateParent(GroupForm form, Group parent, BindingResult bindingResult) {
    if (form.getType() == null) {
      return;
    }
    if (form.getType() == GroupType.COMPANY || form.getType() == GroupType.CLIENT) {
      if (parent != null) {
        bindingResult.rejectValue("parentId", "group.parent.invalid");
      }
      return;
    }
    if (form.getType() == GroupType.DEPARTMENT) {
      if (parent == null || parent.getType() != GroupType.COMPANY) {
        bindingResult.rejectValue("parentId", "group.parent.invalid");
      }
      return;
    }
    if (form.getType() == GroupType.PROJECT) {
      if (parent == null || (parent.getType() != GroupType.DEPARTMENT
          && parent.getType() != GroupType.CLIENT)) {
        bindingResult.rejectValue("parentId", "group.parent.invalid");
      }
    }
  }

  private String msg(String code) {
    Locale locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage(code, null, locale);
  }
}
