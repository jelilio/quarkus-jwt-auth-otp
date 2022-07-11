package io.github.jelilio.jwtauthotp.util;

import io.quarkus.panache.common.Page;

import java.util.List;

public class Paged<T> {
  public List<T> content;
  public MetaFields meta = new MetaFields();

  public Paged(Page page, int totalPages, long totalElements, List<T> content) {
    this.content = content;
    this.meta.size = page.size;
    this.meta.page = page.index;
    this.meta.numberOfElements = content.size();
    this.meta.totalPages = totalPages;
    this.meta.totalElements = totalElements;
  }

  public Paged(MetaFields meta, List<T> content) {
    this.meta = meta;
    this.content = content;
  }

  public static class MetaFields {
    public int size;
    public int page;
    public int numberOfElements;
    public int totalPages;
    public long totalElements;
  }
}
