package api.order.dao;

import api.order.model.Order;
import api.order.repository.OrderRepository;
import jdk.internal.jline.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Constrains server side data access, to limit what the controller is able to do.
 */
@Service
public class OrderDAO {
	@Autowired
	private OrderRepository orderRepository;

	/**
	 * Saves an order to the database, and then returns the saved order.
	 *
	 * @param order Order to be saved
	 * @return Order saved (which should be equivalent to the original order)
	 */
	public Order save(Order order) {
		return orderRepository.save(order);
	}

	/**
	 * Attempts to find an order with a given ID in the database. If the order is found, it will be returned.
	 * Otherwise, null will be returned.
	 *
	 * @param id ID of order to search for
	 * @return Requested order, if it exists, or null otherwise
	 */
	@Nullable
	public Order findById(Long id) {
		return orderRepository.findById(id).orElse(null);
	}

	/**
	 * Deletes an order with the given ID from the database.
	 *
	 * @param id ID of the order to be deleted
	 */
	public void deleteById(Long id) {
		orderRepository.deleteById(id);
	}
}
