package com.example.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryForm {

  private Long id;

  @NotBlank(message = "カテゴリ名は必須です。")
  @Size(max = 50, message = "カテゴリ名は50文字以内で入力してください。")
  private String name;

  @NotBlank(message = "色は必須です。")
  @Size(max = 20, message = "色は20文字以内で入力してください。")
  private String color;
}
