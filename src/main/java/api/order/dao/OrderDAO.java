package api.order.dao;

import api.order.model.Order;
import api.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDAO {
	@Autowired
	private OrderRepository orderRepository;

	public Order save(Order order) {
		return orderRepository.save(order);
	}

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public Order findById(Long id) {
		return orderRepository.getOne(id);
	}

	public void deleteById(Long id) {
		orderRepository.deleteById(id);
	}
}
