package io.github.jelilio.jwtauthotp.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.panache.common.Page;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ResponseWrapper {
  public Object content;
  public String message;
  public MetaFields meta = new MetaFields();

  private ResponseWrapper() {}

  private ResponseWrapper(String message) {
    this.meta = null;
    this.message = message;
  }

  private ResponseWrapper(Page page, int totalPages, long totalElements, List<?> content) {
    this.content = content;
    this.meta.size = page.size;
    this.meta.number = page.index;
    this.meta.numberOfElements = content.size();
    this.meta.totalPages = totalPages;
    this.meta.totalElements = totalElements;
  }

  public static ResponseWrapper of(String message) {
    return new ResponseWrapper(message);
  }
  public static ResponseWrapper of(Page page, int totalPages, long totalElements, List<?> content) {
    return new ResponseWrapper(page, totalPages, totalElements, content);
  }

  public static class MetaFields {
    public int size;
    public int number;
    public int numberOfElements;
    public int totalPages;
    public long totalElements;
  }
}
