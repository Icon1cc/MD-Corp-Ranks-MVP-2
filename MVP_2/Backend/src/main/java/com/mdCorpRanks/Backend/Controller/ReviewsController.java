package com.mdCorpRanks.Backend.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mdCorpRanks.Backend.Service.*;
import com.mdCorpRanks.Backend.Utils.*;
import com.mdCorpRanks.Backend.Model.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import java.util.Collections;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewService reviewService; 
    private final CookieHandling cookieHandling;

    @GetMapping
    public ResponseEntity<?> getReviewAverage(HttpServletRequest request) {
        UUID userId = cookieHandling.extractUserIdFromCookies(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Authentication failed. No valid user ID found in cookies."));
        }
    
        try {
            int averageScore = reviewService.calculateWeightedAverage(userId);
            return ResponseEntity.ok(Collections.singletonMap("totalScore", averageScore)); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve average score."));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> trackReviewSubmission(HttpServletRequest request) {
        UUID userId = cookieHandling.extractUserIdFromCookies(request);
        if (userId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse("User ID not found in cookies."));
        }
    
        boolean reviewAlreadySubmitted = reviewService.checkIfReviewAlreadySubmitted(userId);
        if (reviewAlreadySubmitted) {
            return ResponseEntity.ok(new ApiResponse("Review already submitted for user ID: " + userId));
        }
    
        reviewService.logReviewSubmission(userId);
        return ResponseEntity.ok(new ApiResponse("Review submission tracked successfully."));
    }
}

