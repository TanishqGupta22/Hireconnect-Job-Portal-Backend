package com.hireconnect.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "subscription-service")
public interface SubscriptionClient {

    @GetMapping("/subscriptions/check-limit")
    Boolean checkLimit(@RequestParam Long userId, @RequestParam String userRole);

    @PostMapping("/subscriptions/increment-usage")
    void incrementUsage(@RequestParam Long userId, @RequestParam String userRole);
}
