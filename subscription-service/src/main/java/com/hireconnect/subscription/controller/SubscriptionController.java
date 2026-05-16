package com.hireconnect.subscription.controller;

import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;
import com.hireconnect.subscription.entity.SubscriptionPlan;
import com.hireconnect.subscription.service.SubscriptionService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateSubscription(
            @RequestParam Long userId,
            @RequestParam String userRole,
            @RequestParam String plan) {
        log.info("Initiating subscription request: userId={}, userRole={}, plan={}", userId, userRole, plan);
        try {
            SubscriptionPlan planEnum = SubscriptionPlan.valueOf(plan.toUpperCase());
            if (planEnum == SubscriptionPlan.FREE) {
                return ResponseEntity.ok("FREE_PLAN");
            }
            String orderId = subscriptionService.initiateSubscription(userId, userRole, planEnum);
            return ResponseEntity.ok(orderId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid plan provided: {}", plan);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid plan: " + plan);
        } catch (Exception e) {
            log.error("Failed to initiate subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<SubscriptionResponse> completeSubscription(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.completeSubscription(request));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription(
            @RequestParam Long userId,
            @RequestParam String userRole) {
        log.info("Fetching current subscription: userId={}, userRole={}", userId, userRole);
        try {
            return ResponseEntity.ok(subscriptionService.getCurrentSubscription(userId, userRole));
        } catch (Exception e) {
            log.error("Failed to fetch current subscription: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/check-limit")
    public ResponseEntity<Boolean> checkLimit(
            @RequestParam Long userId,
            @RequestParam String userRole) {
        return ResponseEntity.ok(subscriptionService.canApplyOrPost(userId, userRole));
    }

    @PostMapping("/increment-usage")
    public ResponseEntity<Void> incrementUsage(
            @RequestParam Long userId,
            @RequestParam String userRole) {
        subscriptionService.incrementUsage(userId, userRole);
        return ResponseEntity.ok().build();
    }
}
