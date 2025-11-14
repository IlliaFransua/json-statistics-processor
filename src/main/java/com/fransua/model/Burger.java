package com.fransua.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Burger(UUID id, String name, List<Ingredient> ingredients) {

  @Override
  public String toString() {
    return "Burger{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", ingredients=" + ingredients +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Burger burger = (Burger) o;
    return Objects.equals(id, burger.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
