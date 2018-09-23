package api.order.controller;

import api.catalog.dao.CatalogDAO;
import api.order.dao.OrderDAO;
import api.order.model.Order;
import jdk.internal.jline.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Handles client-server interactions, and specifies what the client can request from the server. Also sanitizes input,
 * to avoid invalid orders.
 */
@RestController
@RequestMapping("/api")
public class OrderContoller {
	@Autowired
	private OrderDAO orderDAO = new OrderDAO();

	@Autowired
	private CatalogDAO catalogDAO = new CatalogDAO();

	/**
	 * Determines if an order contains items that are listen in the catalog. If any invalid item is detected, the
	 * method will immediately return 'false';
	 *
	 * @param order Order to verify
	 * @return True is the order is valid, false if it contains at least one item not listen in the catalog
	 */
	private boolean areOrderItemsValid(Order order) {
		for (Long l : order.getItemIDs())
			if (catalogDAO.findById(l) == null)
				return false;
		return true;
	}

	/**
	 * General order sanitizer, checks that order items are valid and that there are as many items as there are item
	 * quantities.
	 *
	 * @param order Order to verify
	 * @return If any anomaly is detected, a ResponseEntity with the appropriate error code and body is returned.
	 */
	@Nullable
	private ResponseEntity<Object> isOrderValid(@NotNull Order order) {
		if (order.getItemIDs().length != order.getQuantities().length) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
					"Number of items and item quantities in order do not match"
			);
		} else if (!areOrderItemsValid(order)) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
					"Order contained an item with an invalid ID"
			);
		}

		return null;
	}

	/**
	 * Stores an order in the database, if it is valid, as determined by isOrderValid(). If it is valid, it will return
	 * the stored object in the body of the response, otherwise it will return a response with a relevant error code
	 * and information regarding why it was not valid in the body of the response.
	 *
	 * @param order Order to store in the database
	 * @return 200 OK response with the stored order in the body, or a response with relevant error code and
	 * information, contained in the body of the response
	 */
	@PostMapping("/orders")
	public ResponseEntity<Object> createOrder(@Valid @RequestBody Order order) {
		ResponseEntity<Object> response = isOrderValid(order);
		if (response != null)
			return response;

		return ResponseEntity.ok().body(orderDAO.save(order));
	}

	/**
	 * Attempts to retrieve an order from the database. If the order exists, it will return it in a 200 OK response,
	 * with the requested order in the body of the response. Otherwise, a 404 Not Found response will be issued.
	 *
	 * @param id ID of order to retrieve from the databse
	 * @return 200 OK response with order in the body if it exists, 404 Not Found response otherwise
	 */
	@GetMapping("/orders/{id}")
	public ResponseEntity<Order> getCatalogEntryById(@PathVariable(value = "id") Long id) {
		Order order = orderDAO.findById(id);

		if (order == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok().body(order);
	}

	/**
	 * Attempts to replace an old order in the database with a new one. If the order being replaced does not exist, a
	 * 404 Not Found response is issued. If the order exists, but the new order is deemed invalid by isOrderValid(),
	 * a response with a relevant error code and information in the body will be issued. If the order exists, and the
	 * new order is valid, the old order will be overwritten, and a 200 OK response will be issued with the updated
	 * order in the body of the response.
	 *
	 * @param id       ID of order to replace
	 * @param newOrder New order, which will overwrite the one with the specified ID
	 * @return 404 Not Found if order to be replaced does not exist, relevant error code and information in body if
	 * isOrderValid() deems the new order invalid, or 200 OK with the updated order in the body if all is good
	 */
	@PutMapping("/orders/{id}")
	public ResponseEntity<Object> updateCatalogEntry(@PathVariable(value = "id") Long id, @Valid @RequestBody
			Order newOrder) {
		Order order = orderDAO.findById(id);

		if (order == null)
			return ResponseEntity.notFound().build();

		ResponseEntity<Object> response = isOrderValid(newOrder);
		if (response != null)
			return response;

		order.setPurchaserName(newOrder.getPurchaserName());
		order.setItemIDs(newOrder.getItemIDs());
		order.setQuantities(newOrder.getQuantities());

		Order updatedEntry = orderDAO.save(order);
		return ResponseEntity.ok().body(updatedEntry);
	}

	/**
	 * Deletes an order with the specified ID from the database. If the order to be deleted does not exist, a 404 Not
	 * Found response is issued. If the order exists and has been deleted, a 200 OK response is issued.
	 *
	 * @param id ID of order to delete
	 * @return 404 Not Found response if order to be deleted does not exist, 200 OK response otherwise
	 */
	@DeleteMapping("/orders/{id}")
	public ResponseEntity<Order> deleteCatalogEntry(@PathVariable(value = "id") Long id) {
		Order order = orderDAO.findById(id);

		if (order == null)
			return ResponseEntity.notFound().build();

		orderDAO.deleteById(id);
		return ResponseEntity.ok().build();
	}
}
