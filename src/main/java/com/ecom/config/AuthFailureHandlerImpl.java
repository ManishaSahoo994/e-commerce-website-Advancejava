package com.ecom.config;

import java.io.IOException;

import javax.security.auth.login.AccountException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepo;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler{
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String email = request.getParameter("username");
		
		UserDtls userDtls = userRepo.findByEmail(email);
		
		if(userDtls != null) {
			
		if(userDtls.getIsEnable())
		{
			if(userDtls.getAccountNonLocked())
			{
				if(userDtls.getFailedAttempt()<AppConstant.ATTEMPT_TIME)
				{
					userService.increaseFailedAttempt(userDtls);
				}else {
					userService.userAccountLock(userDtls);
					exception=new LockedException("your account is locked || failed attempt 3");
				}
				
			}else {
				
				if(userService.unlockAccountTimeExpired(userDtls))
				{
					exception=new LockedException("your Account is unlocked || please try to login");
				}else {
					exception=new LockedException("your Account is locked || please try after sometimes");
				}
				
				exception=new LockedException("your Account is locked");
			}
			
		}else {
			exception=new LockedException("your Account is inactive");
		}
		}else {
			exception=new LockedException("Email & password is invalid");
		}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}
