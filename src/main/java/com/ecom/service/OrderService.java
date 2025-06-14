package com.ecom.service;

import java.util.List;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

public interface OrderService {
	
	public void saveOrder(Integer userid,OrderRequest orderRequest) throws Exception;
	
	public List<ProductOrder> getOrderByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id,String status);
	
	public List<ProductOrder> getAllOrders();
	
	public ProductOrder getOrdersByOrderId(String orderId);
     
}
