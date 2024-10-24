package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.form.FavoriteRegisterForm;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;

@Controller
@RequestMapping("/houses")
public class HouseController {
	private final HouseRepository houseRepository;
	private final ReviewRepository reviewRepository;
	private final FavoriteRepository favoriteRepository;
	
	public HouseController(HouseRepository houseRepository,
			ReviewRepository reviewRepository, FavoriteRepository favoriteRepository) {
		this.houseRepository = houseRepository;
		this.reviewRepository = reviewRepository;
		this.favoriteRepository = favoriteRepository;
	}

	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			//検索部分のパラメーター
			@RequestParam(name = "area", required = false) String area,
			@RequestParam(name = "price", required = false) Integer price,
			@RequestParam(name = "order", required = false) String order,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {
		Page<House> housePage;
		//キーワードによる検索
		if (keyword != null && !keyword.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%",
						"%" + keyword + "%", pageable);
			} else {
				housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%",
						"%" + keyword + "%", pageable);
			}
		//エリアによる検索
		} else if (area != null && !area.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
			} else {
				housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
			}
		//価格による検索
		} else if (price != null) {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
			} else {
				housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		//その他
		} else {
			if (order != null && order.equals("priceAsc")) {
				housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
			} else {
				housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);
			}
		}

		model.addAttribute("housePage", housePage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("area", area);
		model.addAttribute("price", price);
		model.addAttribute("order", order);

		return "houses/index";
	}
	


	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
	                   FavoriteRegisterForm favoriteRegisterForm,
	                   FavoriteRepository favoriteRepository,
	                   Model model,
	                   @PageableDefault(page = 0, size = 6, sort = "id", direction = Direction.ASC) Pageable pageable,
	                   @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
	    
	    // Houseを取得
	    House house = houseRepository.getReferenceById(id);
	    
	    // houseに基づいてレビューを取得
	    Page<Review> reviewPage = reviewRepository.findByHouseOrderByCreatedAtDesc(house, pageable);
        
	    // レビューが見つからなかった場合の処理
	    if (reviewPage.isEmpty()) {
	        System.out.println("No reviews found for house id: " + id);
	    }

	    ReservationInputForm reservationInputForm = new ReservationInputForm();

	    // モデルにデータを追加
	    model.addAttribute("reviewPage", reviewPage); 
	    model.addAttribute("house", house);
	    model.addAttribute("reservationInputForm", reservationInputForm);
	  
	 /// ユーザー情報の追加
	    if (userDetailsImpl != null) {
	        // ログインユーザ情報の取得
	        var user = userDetailsImpl.getUser();
	        model.addAttribute("user", user);
	        
	        // ログインユーザが登録したレビューが存在しないことをチェック
	        var hasNotMyReview = reviewPage.filter(review -> review.getUser().getId().equals(userDetailsImpl.getUser().getId())).isEmpty();
	        // 上記のチェック結果をviwにわたす。
	        model.addAttribute("hasNotMyReview", hasNotMyReview);
	        
	        var favorite = this.favoriteRepository.findByHouseAndUser(house, user);
	        if (favorite == null) {
	            model.addAttribute("favoriteId", null);
	        } else {
	            model.addAttribute("favoriteId", favorite.getId());
	        }

        

	        // ログインしていないときは、そもそもボタンの表示がされないが、念の為trueを設定しておく
	        model.addAttribute("hasNotMyReview", true);
	    }

	    return "houses/show";
	}
	
}