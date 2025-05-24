package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.repository.CartRepo;
import com.ecom.repository.ProductOrderRepo;
import com.ecom.service.OrderService;
import com.ecom.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private ProductOrderRepo orderRepo;
	
	@Autowired
	private CartRepo cartRepo;

	@Override
	public void saveOrder(Integer userid,OrderRequest orderRequest) {
		List<Cart> carts = cartRepo.findByUserId(userid);
		
		for(Cart cart:carts)
		{
		ProductOrder order = new ProductOrder();
		
		order.setOrderId(UUID.randomUUID().toString());
		order.setOrderDate(LocalDate.now());
		
		order.setProduct(cart.getProduct());
		order.setPrice(cart.getProduct().getDiscountPrice());
		
		order.setQuantity(cart.getQuantity());
		order.setUser(cart.getUser());
		
		order.setStatus(OrderStatus.IN_PROGRESS.getName());
		order.setPaymentType(orderRequest.getPaymentType());
		
		OrderAddress address=new OrderAddress();
		
		address.setFirstName(orderRequest.getFirstName());
		address.setLastName(orderRequest.getLastName());
		address.setEmail(orderRequest.getEmail());
		address.setPhoneNo(orderRequest.getPhoneNo());
		address.setAddress(orderRequest.getAddress());
		address.setCity(orderRequest.getCity());
		address.setState(orderRequest.getState());
		address.setPincode(orderRequest.getPincode());
		
		order.setOrderAddress(address);
		
		orderRepo.save(order);
		
		
		
		}
	}

	@Override
	public List<ProductOrder> getOrderByUser(Integer userId) {
		List<ProductOrder> orders = orderRepo.findByUserId(userId);
		return orders;
	}

	@Override
	public Boolean updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepo.findById(id);
		if(findById.isPresent())
		{
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			orderRepo.save(productOrder);
			return true;
		}
		return false;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepo.findAll();
	}

}
