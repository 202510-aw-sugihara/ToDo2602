package com.example.todo;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoryController {

  private final CategoryRepository categoryRepository;

  public CategoryController(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @GetMapping("/categories")
  public String list(Model model) {
    List<Category> categories = categoryRepository.findAll();
    model.addAttribute("categories", categories);
    return "category/list";
  }

  @GetMapping("/categories/new")
  public String newCategory(@ModelAttribute("categoryForm") CategoryForm form) {
    return "category/new";
  }

  @PostMapping("/categories")
  public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "category/new";
    }
    Category category = new Category();
    category.setName(form.getName());
    category.setColor(form.getColor());
    categoryRepository.save(category);
    redirectAttributes.addFlashAttribute("successMessage", "カテゴリを作成しました。");
    return "redirect:/categories";
  }

  @GetMapping("/categories/{id}/edit")
  public String edit(@PathVariable("id") long id, Model model) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    model.addAttribute("categoryForm", new CategoryForm(category.getId(),
        category.getName(), category.getColor()));
    return "category/edit";
  }

  @PostMapping("/categories/{id}")
  public String update(@PathVariable("id") long id,
      @Valid @ModelAttribute("categoryForm") CategoryForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "category/edit";
    }
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    category.setName(form.getName());
    category.setColor(form.getColor());
    categoryRepository.save(category);
    redirectAttributes.addFlashAttribute("successMessage", "カテゴリを更新しました。");
    return "redirect:/categories";
  }
}
