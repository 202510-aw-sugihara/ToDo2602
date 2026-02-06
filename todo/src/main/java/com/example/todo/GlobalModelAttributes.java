package com.example.todo;

import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

  private final MessageSource messageSource;
  private final List<String> supportedLangs = List.of("ja", "en", "zh");

  public GlobalModelAttributes(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ModelAttribute("langOptions")
  public List<LangOption> langOptions() {
    Locale locale = LocaleContextHolder.getLocale();
    return supportedLangs.stream()
        .map(code -> new LangOption(code, messageSource.getMessage("lang." + code, null, code, locale)))
        .toList();
  }

  @ModelAttribute("currentLang")
  public String currentLang() {
    return LocaleContextHolder.getLocale().getLanguage();
  }

  @ModelAttribute("currentLangLabel")
  public String currentLangLabel() {
    Locale locale = LocaleContextHolder.getLocale();
    String code = locale.getLanguage();
    return messageSource.getMessage("lang." + code, null, code, locale);
  }

  public record LangOption(String code, String label) {}
}
