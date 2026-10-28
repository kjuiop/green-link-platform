package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.FindIdResult;
import com.greenlink.core.application.auth.usecase.FindIdByPhoneUseCase;
import com.greenlink.core.application.auth.usecase.ResetPasswordUseCase;
import com.greenlink.core.application.auth.usecase.SendPhoneVerificationUseCase;
import com.greenlink.core.application.auth.usecase.VerifyPhoneCodeUseCase;
import com.greenlink.core.application.sms.dto.SmsResult;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.phoneverification.model.PhoneVerification;
import com.greenlink.core.domain.phoneverification.port.PhoneVerificationRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import com.greenlink.core.domain.sms.port.SmsClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PhoneVerificationService implements
        SendPhoneVerificationUseCase,
        VerifyPhoneCodeUseCase,
        FindIdByPhoneUseCase,
        ResetPasswordUseCase {

    private final PhoneVerificationRepository verificationRepository;
    private final AdminRepository adminRepository;
    private final SmsClient smsClient;
    private final PasswordEncoder passwordEncoder;

    public PhoneVerificationService(PhoneVerificationRepository verificationRepository,
                                    AdminRepository adminRepository,
                                    SmsClient smsClient,
                                    PasswordEncoder passwordEncoder) {
        this.verificationRepository = verificationRepository;
        this.adminRepository = adminRepository;
        this.smsClient = smsClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void send(String phone) {
        PhoneVerification verification = PhoneVerification.create(phone);
        verificationRepository.save(verification);

        String msg = "[온우림] 인증번호: " + verification.getCode() + " (3분 이내 입력)";
        SmsResult result = smsClient.send(phone, msg, null, null, null, null, null);
        if (!result.success()) {
            throw new BusinessException(AuthErrorCode.SMS_SEND_FAILED);
        }
    }

    @Override
    public void verify(String phone, String code) {
        PhoneVerification verification = verificationRepository.findLatestByPhone(phone)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.PHONE_CODE_INVALID));

        if (!verification.matchesCode(code)) {
            throw new BusinessException(AuthErrorCode.PHONE_CODE_INVALID);
        }

        verification.markVerified();
        verificationRepository.save(verification);
    }

    @Override
    public FindIdResult findId(String phone) {
        PhoneVerification verification = verificationRepository.findLatestByPhone(phone)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.PHONE_NOT_VERIFIED));

        if (!verification.isVerifiedAndAvailable()) {
            throw new BusinessException(AuthErrorCode.PHONE_NOT_VERIFIED);
        }

        Admin admin = adminRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND_BY_PHONE));

        verification.markUsed();
        verificationRepository.save(verification);

        return new FindIdResult(admin.getLoginId(), admin.getCreatedAt());
    }

    @Override
    public void reset(String phone, String loginId, String newPassword) {
        PhoneVerification verification = verificationRepository.findLatestByPhone(phone)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.PHONE_NOT_VERIFIED));

        if (!verification.isVerifiedAndAvailable()) {
            throw new BusinessException(AuthErrorCode.PHONE_NOT_VERIFIED);
        }

        Admin admin = adminRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND_BY_PHONE));

        if (!admin.getLoginId().equals(loginId)) {
            throw new BusinessException(AuthErrorCode.MEMBER_NOT_FOUND_BY_PHONE);
        }

        adminRepository.updatePassword(admin.getId(), passwordEncoder.encode(newPassword));

        verification.markUsed();
        verificationRepository.save(verification);
    }
}
