package com.project.revhive.social.client;

import com.project.revhive.social.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{userId}/summary")
    UserSummary getUser(@PathVariable("userId") Long userId);
}
