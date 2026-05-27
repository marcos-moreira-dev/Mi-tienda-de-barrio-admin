package com.marcosmoreira.mitiendadebarrio.admin.shared.result;

import java.util.List;

/** Resultado paginado local para tablas JavaFX. */
public record PageResult<T>(List<T> items, int page, int pageSize, long totalItems) {
}
