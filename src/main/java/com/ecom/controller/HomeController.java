package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

@Controller
public class HomeController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m)
    {
	if(p!=null)
	{
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		m.addAttribute("user", userDtls);
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
	
}
