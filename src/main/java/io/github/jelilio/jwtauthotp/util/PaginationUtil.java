package io.github.jelilio.jwtauthotp.util;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;

import java.util.List;

public abstract class PaginationUtil {
  public static <T> Uni<Paged<T>> paginate(Page page, PanacheQuery<T> query) {
    Uni<List<T>> uniContents = query.list();
    Uni<Integer> uniTotalPages = query.pageCount();
    Uni<Long> uniTotalElements = query.count();

    return uniContents.flatMap(content -> Uni.combine().all()
        .unis(uniTotalPages, uniTotalElements).asTuple().map(item -> {
          var totalPages = item.getItem1();
          var totalElements = item.getItem2();
          return new Paged<>(page, totalPages, totalElements, content);
        })
    );
  }
}
