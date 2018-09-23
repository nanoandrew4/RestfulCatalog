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
import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderContoller {
	@Autowired
	private OrderDAO orderDAO = new OrderDAO();

	@Autowired
	private CatalogDAO catalogDAO = new CatalogDAO();

	private boolean areOrderItemsValid(Order order) {
		for (Long l : order.getItemIDs())
			if (l > catalogDAO.getNumOfElements())
				return false;
		return true;
	}

	@Nullable
	private ResponseEntity<Object> isOrderValid(@NotNull Order order) {
		if (!areOrderItemsValid(order)) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
					"Order contained an item with an invalid ID"
			);
		}

		if (order.getItemIDs().length != order.getQuantities().length) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
					"Number of items and item quantities in order do not match"
			);
		}

		return null;
	}

	@PostMapping("/orders")
	public ResponseEntity<Object> createCatalogEntry(@Valid @RequestBody Order order) {
		ResponseEntity<Object> response = isOrderValid(order);
		if (response != null)
			return response;

		return ResponseEntity.ok().body(orderDAO.save(order));
	}

	@GetMapping("/orders")
	public List<Order> getAllCatalogEntries() {
		return orderDAO.findAll();
	}

	@GetMapping("/orders/{id}")
	public ResponseEntity<Order> getCatalogEntryById(@PathVariable(value = "id") Long id) {
		Order order = orderDAO.findById(id);

		if (order == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok().body(order);
	}

	@PutMapping("/orders/{id}")
	public ResponseEntity<Object> updateCatalogEntry(@PathVariable(value = "id") Long id, @Valid @RequestBody
			Order newOrder) {
		Order order = orderDAO.findById(id);

		if (order == null)
			return ResponseEntity.notFound().build();

		ResponseEntity<Object> response = isOrderValid(order);
		if (response != null)
			return response;

		order.setPurchaserName(newOrder.getPurchaserName());
		order.setItemIDs(newOrder.getItemIDs());
		order.setQuantities(newOrder.getQuantities());

		Order updatedEntry = orderDAO.save(order);
		return ResponseEntity.ok().body(updatedEntry);
	}

	@DeleteMapping("/orders/{id}")
	public ResponseEntity<Order> deleteCatalogEntry(@PathVariable(value = "id") Long id) {
		Order order = orderDAO.findById(id);

		if (order == null)
			return ResponseEntity.notFound().build();

		orderDAO.deleteById(id);
		return ResponseEntity.ok().build();
	}
}
