package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.BusinessNoExistsResponse;
import com.greenlink.shop.adapter.in.web.dto.SignupRequest;
import com.greenlink.shop.adapter.in.web.dto.SignupResponse;
import com.greenlink.shopcore.application.auth.usecase.ShopSignupUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ShopSignupController {

    private final ShopSignupUseCase signupUseCase;
    private final ClientIpResolver clientIp;

    public ShopSignupController(ShopSignupUseCase signupUseCase, ClientIpResolver clientIp) {
        this.signupUseCase = signupUseCase;
        this.clientIp = clientIp;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request, HttpServletRequest http) {
        return SignupResponse.from(signupUseCase.signup(request.toCommand(), clientIp.resolve(http)));
    }

    @GetMapping("/business-numbers/{businessNo}/exists")
    public BusinessNoExistsResponse businessNoExists(@PathVariable String businessNo, HttpServletRequest http) {
        return new BusinessNoExistsResponse(signupUseCase.businessNoExists(businessNo, clientIp.resolve(http)));
    }
}
