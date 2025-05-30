package com.ecom.util;

public enum OrderStatus {
	
	IN_PROGRESS(1,"In progress"),
	ORDER_RECEIVED(2,"Order Recived"),
	PRODUCT_PACKED(3,"Product Packed"),
	OUT_FOR_DELIVERY(4,"Out for delivery"),
	DELIVERED(5,"Delivered"),
	CANCEL(6,"Cancelled"),
	SUCCESS(7,"Success");
	
	private Integer id;
	
	private String name;

	private OrderStatus(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	

}
