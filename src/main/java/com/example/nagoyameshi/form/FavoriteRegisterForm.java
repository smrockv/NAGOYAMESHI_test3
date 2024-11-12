package com.example.nagoyameshi.form;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FavoriteRegisterForm {
	
	@NotNull
	private Integer restaurantId;
	
	@NotNull
	private Integer userId;
	

	@Transactional
	public void deleteByRestaurantIdAndUserId(Integer restaurantId, Integer userId) {
		this.restaurantId = restaurantId;
		this.userId = userId;
	}
}