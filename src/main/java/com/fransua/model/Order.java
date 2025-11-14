package com.fransua.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Order(UUID id, Instant createdAt, List<Burger> burgers) {

  @Override
  public String toString() {
    return "Order{" +
        "id=" + id +
        ", createdAt=" + createdAt +
        ", burgers=" + burgers +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Order order = (Order) o;
    return Objects.equals(id, order.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
