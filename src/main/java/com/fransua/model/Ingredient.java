package com.fransua.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record Ingredient(UUID id, String name, BigDecimal unitPrice) {

  @Override
  public String toString() {
    return "Ingredient{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", unitPrice=" + unitPrice +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ingredient that = (Ingredient) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
