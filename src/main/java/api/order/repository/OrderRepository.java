package api.order.repository;

import api.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Handles database operations relating to Orders, and more specifically, the 'orders' table in the database.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
