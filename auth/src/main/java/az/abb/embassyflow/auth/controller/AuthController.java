package az.abb.embassyflow.auth.controller;

import az.abb.embassyflow.auth.dto.request.FinVerifyRequest;
import az.abb.embassyflow.auth.dto.request.OtpSendRequest;
import az.abb.embassyflow.auth.dto.request.OtpValidateRequest;
import az.abb.embassyflow.auth.dto.response.FinVerifyResponse;
import az.abb.embassyflow.auth.dto.response.OtpSendResponse;
import az.abb.embassyflow.auth.dto.response.OtpValidateResponse;
import az.abb.embassyflow.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/fin/verify")
    public FinVerifyResponse verifyFin(@Valid @RequestBody FinVerifyRequest request) {
        return authService.verifyFin(request);
    }

    @PostMapping("/otp/send")
    public OtpSendResponse sendOtp(@Valid @RequestBody OtpSendRequest request) {
        return authService.sendOtp(request);
    }

    @PostMapping("/otp/validate")
    public OtpValidateResponse validateOtp(@Valid @RequestBody OtpValidateRequest request) {
        return authService.validateOtp(request);
    }
}