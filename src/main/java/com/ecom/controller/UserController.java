package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CartService cartService;
	
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
    	return "/user/cart";
    }

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
    	UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
}
