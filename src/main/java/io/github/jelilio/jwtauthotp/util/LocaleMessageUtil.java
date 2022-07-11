package io.github.jelilio.jwtauthotp.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class LocaleMessageUtil {
  private static String getMessage(String lang, String key) {
    Locale locale = Locale.forLanguageTag(lang);

    if(locale == null) locale = Locale.ENGLISH;

    String message = key;
    try {
      ResourceBundle labels = ResourceBundle.getBundle("messages", locale);
      message = labels.getString(key);
    } catch (Exception ignored) {}

    return message;
  }

  public static String getMessage(String lang, String key, Object... input) {
    return MessageFormat.format(getMessage(lang, key), input);
  }

  public static String getDefaultMessage(String key, Object... input) {
    return MessageFormat.format(getMessage("en", key), input);
  }
}
