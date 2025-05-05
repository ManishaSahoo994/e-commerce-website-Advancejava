package com.ecom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.UserDtls;

public interface UserRepo extends JpaRepository<UserDtls, Integer>{
	

}
