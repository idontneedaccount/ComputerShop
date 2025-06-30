package com.example.computershop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.computershop.dto.ProductRatingDTO;
import com.example.computershop.dto.ReviewDTO;
import com.example.computershop.entity.Review;
import com.example.computershop.repository.ReviewRepository;
import com.example.computershop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    
    /**
     * Convert Review entity to ReviewDTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO.UserInfo userInfo = null;
        
        if (review.getUser() != null) {
            userInfo = ReviewDTO.UserInfo.builder()
                    .userId(review.getUser().getUserId())
                    .fullName(review.getUser().getFullName())
                    .image(review.getUser().getImage())
                    .build();
        }
        
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId())
                .productId(review.getProductId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .user(userInfo)
                .build();
    }

    /**
     * Lấy thống kê rating của sản phẩm
     */
    public ProductRatingDTO getProductRating(String productId) {
        try {
            ProductRatingDTO ratingDTO = ProductRatingDTO.builder()
                    .productId(productId)
                    .averageRating(reviewRepository.getAverageRatingByProductId(productId))
                    .totalReviews(reviewRepository.getTotalReviewsByProductId(productId))
                    .build();
            
            // Lấy phân bố rating
            List<Object[]> distribution = reviewRepository.getRatingDistributionByProductId(productId);
            Map<Integer, Long> ratingMap = new HashMap<>();
            
            for (Object[] row : distribution) {
                Integer rating = (Integer) row[0];
                Long count = (Long) row[1];
                ratingMap.put(rating, count);
            }
            ratingDTO.setRatingDistribution(ratingMap);
            
            return ratingDTO;
        } catch (Exception e) {
            logger.error("Error getting product rating for productId: {}", productId, e);
            return ProductRatingDTO.builder()
                    .productId(productId)
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .ratingDistribution(new HashMap<>())
                    .build();
        }
    }
    
    /**
     * Lấy danh sách reviews của sản phẩm (return DTO)
     */
    public Page<ReviewDTO> getProductReviewsDTO(String productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return reviews.map(this::convertToDTO);
    }
    
    /**
     * Lấy danh sách reviews của sản phẩm (return Entity - for internal use)
     */
    public Page<Review> getProductReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }
    
    /**
     * Lấy reviews theo rating filter
     */
    public Page<Review> getProductReviewsByRating(String productId, Integer rating, Pageable pageable) {
        return reviewRepository.findByProductIdAndRatingOrderByCreatedAtDesc(productId, rating, pageable);
    }
    
    /**
     * Kiểm tra user có thể review sản phẩm không
     */
    public boolean canUserReview(String userId, String productId) {
        if (userId == null || productId == null) return false;
        
        // Kiểm tra đã review chưa
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            return false;
        }
        
        // TODO: Kiểm tra user đã mua sản phẩm chưa (implement later)
        // boolean hasPurchased = orderService.hasUserPurchasedProduct(userId, productId);
        
        return true;
    }
    
    /**
     * Tạo review mới
     */
    @Transactional
    public Review createReview(String userId, String productId, Integer rating, String comment) {
        try {
            // Validation
            if (!canUserReview(userId, productId)) {
                throw new IllegalArgumentException("User cannot review this product");
            }
            
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            Review review = Review.builder()
                    .userId(userId)
                    .productId(productId)
                    .rating(rating)
                    .comment(comment)
                    .build();
            
            return reviewRepository.save(review);
        } catch (Exception e) {
            logger.error("Error creating review for user: {} product: {}", userId, productId, e);
            throw new RuntimeException("Failed to create review", e);
        }
    }
    
    /**
     * Cập nhật review
     */
    @Transactional
    public Review updateReview(String reviewId, String userId, Integer rating, String comment) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                throw new IllegalArgumentException("Review not found");
            }
            
            Review review = reviewOpt.get();
            
            // Kiểm tra quyền sửa (chỉ người tạo mới được sửa)
            if (!review.getUserId().equals(userId)) {
                throw new IllegalArgumentException("User cannot edit this review");
            }
            
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            review.setRating(rating);
            review.setComment(comment);
            
            return reviewRepository.save(review);
        } catch (Exception e) {
            logger.error("Error updating review: {}", reviewId, e);
            throw new RuntimeException("Failed to update review", e);
        }
    }
    
    /**
     * Xóa review
     */
    @Transactional
    public boolean deleteReview(String reviewId, String userId) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                return false;
            }
            
            Review review = reviewOpt.get();
            
            // Kiểm tra quyền xóa (chỉ người tạo hoặc admin)
            if (!review.getUserId().equals(userId)) {
                // TODO: Check if user is admin
                throw new IllegalArgumentException("User cannot delete this review");
            }
            
            reviewRepository.delete(review);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting review: {}", reviewId, e);
            return false;
        }
    }
    
    /**
     * Lấy review của user cho sản phẩm
     */
    public Optional<Review> getUserReviewForProduct(String userId, String productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId);
    }
} 