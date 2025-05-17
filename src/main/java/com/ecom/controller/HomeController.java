package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;

import org.apache.naming.factory.SendMailFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
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
	public String index(){
		return "index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	@GetMapping("/product")
	public String product(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
		List<Category> categories = categoryService.getAllActiveCategory();
		List<Product> products = productService.getAllActiveProduct(category);
		m.addAttribute("categories", categories);
		m.addAttribute("products", products);
		m.addAttribute("paramValue", category);
		return "product";
	}
	@GetMapping("/product/{id}")
	public String view_product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product",productById);
		return "view_product";
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("profile_image") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
		
		String imageName = file.isEmpty() ? "default.jpg" :file.getOriginalFilename();
		
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveUser(user);
		if(!ObjectUtils.isEmpty(saveUser)) 
		{
			if(!file.isEmpty())
			{
				File saveFile = new ClassPathResource("static/img").getFile();
	        	Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
	        	
	        	//System.out.println(path);
	        	Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
	        	redirectAttributes.addFlashAttribute("SuccMsg", "Register Successfully");
			}else {
				 redirectAttributes.addFlashAttribute("errorMsg", "Not register! Internal server error");
			}
		
		
		return "redirect:/register";
	}
	//forgot password code
	@GetMapping("/forgot_password")
	public String showForgotPassword() {
		return "forgot_password.html";
	}
	
	@PostMapping ("/forgot_password")
	public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		
		UserDtls userByEmail = userService.getUserByEmail(email);
		
		if(ObjectUtils.isEmpty(userByEmail))
		{
			redirectAttributes.addFlashAttribute("errorMsg", "invalid email");
		}else {
			
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email,resetToken);
			
			//generate URL : http://localhost:8080/reset_password?token=ryfyhiolsfhjkhkljlhhjgdfsdghfjhkljl
			
			String url = commonUtil.generateUrl(request)+"/reset_password?token="+resetToken;

			
			Boolean sendMail = commonUtil.sendMail(url, email);
			
			if(sendMail)
			{
				redirectAttributes.addFlashAttribute("SuccMsg", "Please check your email...Password reset link sent.");
			}else {
				redirectAttributes.addFlashAttribute("errorMsg", "Something wrong on server! Email can not sent.");
			}
			}
		return "redirect:/forgot_password";
	}
	@GetMapping("/reset_password")
	public String showResetPassword(@RequestParam String token,RedirectAttributes redirectAttributes,Model m) {
		UserDtls userByToken = userService.getUserByToken(token);
		
		if(ObjectUtils.isEmpty(userByToken))
		{
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token",token);
		return "reset_password";
	}
	
	
	@PostMapping("/reset_password")
	public String resetPassword(@RequestParam String token,@RequestParam String password, RedirectAttributes redirectAttributes,Model m) {
		UserDtls userByToken = userService.getUserByToken(token);

		if(ObjectUtils.isEmpty(userByToken))
		{
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			//redirectAttributes.addFlashAttribute("SuccMsg", "Password change successfully.");
			m.addAttribute("msg","Password change successfully");
			return "message";
		}
		
	}
	
}
