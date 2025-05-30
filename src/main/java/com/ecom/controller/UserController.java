package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m)
    {
	if(p!=null)
	{
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		m.addAttribute("user", userDtls);
		Integer countCart = cartService.getCountCart(userDtls.getId());
		m.addAttribute("countCart", countCart);
	}
	List<Category> allActiveCategory = categoryService.getAllActiveCategory();
	m.addAttribute("category", allActiveCategory);
	}
	
	@GetMapping("/")
	public String home() {
		return "user/home";
	}
    @GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid,@RequestParam Integer uid,RedirectAttributes redirectAttributes)
	{
    	Cart saveCart = cartService.saveCart(pid, uid);
    	if(ObjectUtils.isEmpty(saveCart))
    	{
    		redirectAttributes.addFlashAttribute("errorMsg", "Product add to cart failed!!");
    	}else {
    		redirectAttributes.addFlashAttribute("SuccMsg", "Product added to cart successful.");
		}
		return "redirect:/product/" + pid;
	}
    @GetMapping("/cart")
    public String loadCartPage(Principal p, Model m) {
    	
    	UserDtls user = getLoggedInUserDetails(p);
    	List<Cart> carts = cartService.getCartsByUser(user.getId());
    	m.addAttribute("carts",carts);
    	if(carts.size() > 0) {
    	Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
    	m.addAttribute("totalOrderPrice",totalOrderPrice);
    	}
    	return "/user/cart";
    }
    
    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid)
    {
    	cartService.updateQuantity(sy,cid);
    	return "redirect:/user/cart";
    }

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
    	UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	
	@GetMapping("/orders")
	public String orderPage(Principal p,Model m)
	{
		UserDtls user = getLoggedInUserDetails(p);
    	List<Cart> carts = cartService.getCartsByUser(user.getId());
    	m.addAttribute("carts",carts);
    	if(carts.size() > 0) {
    	Double orderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
    	Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice() + 200 + 100;
    	m.addAttribute("orderPrice",orderPrice);
    	m.addAttribute("totalOrderPrice",totalOrderPrice);
    	}
		return "/user/order";
	}
	
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request,Principal p) throws Exception
	{
		//System.out.println(request);
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);
		return "redirect:/user/success";
	}
	@GetMapping("/success")
	public String loadSuccess()
	{
		return "/user/success";
	}
	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p)
	{
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrderByUser(loginUser.getId());
		m.addAttribute("orders",orders);
		return "user/my_orders";
	}
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,RedirectAttributes redirectAttributes)
	{
		OrderStatus[] values = OrderStatus.values();
		String status=null;
		
		for(OrderStatus orderSt:values)
		{
			if(orderSt.getId().equals(st))
			{
				status=orderSt.getName();
			}
		}
		
		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!ObjectUtils.isEmpty(updateOrder))
		{
			redirectAttributes.addFlashAttribute("SuccMsg", "Status updated Successfully");
		}else {
			 redirectAttributes.addFlashAttribute("errorMsg", "Status not updated! Internal server error");
		}
		
		
		return "redirect:/user/user-orders";
	}
	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user,@RequestParam MultipartFile img,RedirectAttributes redirectAttributes) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(updateUserProfile))
		{
			redirectAttributes.addFlashAttribute("errorMsg", "Profile not updated! Internal server error");
		}else {
			redirectAttributes.addFlashAttribute("SuccMsg", "Profile updated successfully!");
		}
		return "redirect:/user/profile";
	}
	@PostMapping("change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,RedirectAttributes redirectAttributes)
	{
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
		
		if(matches)
		{
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			
			if(ObjectUtils.isEmpty(updateUser))
			{
				redirectAttributes.addFlashAttribute("errorMsg", "Password not updated!Error in server.");
			}else {
				redirectAttributes.addFlashAttribute("SuccMsg", "Password updated successfully!");
			}
			
		}else {
			redirectAttributes.addFlashAttribute("errorMsg", "Current password incorrect!");
		}
		
		return "redirect:/user/profile";
	}
}
