package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.Enum.OtpStatusEnum;
import com.ashwinsi.bankingApplication.Domain.Otp;
import com.ashwinsi.bankingApplication.Repository.OtpRepository;
import com.ashwinsi.bankingApplication.Utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class OtpService {
    private final OtpRepository otpRepository;

    // IMP OTP WILL BE CACHED BASED ON THE ACTION ID NO THE ID OF THE OTP
    // IN THE CACHE ITSELF WE CAN STORE THE ATTEMPTS TOO
    private final Map<UUID, Otp> otpCache = new HashMap<>();

    OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;

    }

    private Otp getOtpFromCache(UUID actionId) throws Exception {
        // CACHE MISS
        if (!otpCache.containsKey(actionId)) {
            Otp otp = findOtpFromRepository(actionId);
            otpCache.put(actionId, otp);
        }
        return otpCache.get(actionId);
    }

    private void updateCache(UUID actionId, Otp otp) {
        otpCache.put(actionId, otp);
    }

    private void updateOtpStatus(UUID actionId, OtpStatusEnum otpStatusEnum) throws Exception {
        Otp otp = getOtpFromCache(actionId);
        otp.setStatus(otpStatusEnum);
        otpRepository.save(otp);
    }

    public void isAllowed(UUID actionId) throws Exception {
        Otp otp = getOtpFromCache(actionId);

        if (otp.getIncorrectAttempts() >= otp.getAttemptsAllowed()) {
            updateOtpStatus(actionId, OtpStatusEnum.OTP_ATTEMPTS_EXCEEDED);
            throw new CustomError("OTP Attempts Exceeded", HttpStatus.BAD_REQUEST);
        }

        if (LocalDateTime.now().isAfter(otp.getCreatedAt().plusMinutes(5))) {
            updateOtpStatus(actionId, OtpStatusEnum.OTP_EXPIRED);
            throw new CustomError("OTP Expired", HttpStatus.BAD_REQUEST);
        }
    }

    private void incrementOtpIncorrectAttempts(UUID actionId) throws Exception {
        Otp otp = getOtpFromCache(actionId);
        otp.setIncorrectAttempts(otp.getIncorrectAttempts() + 1);
        otpRepository.save(otp);
    }

    private Integer generateOTP() {
        return (int) (Math.random() * 900000) + 100000;
    }

    public Otp findOtp(UUID actionId) throws Exception {
        return getOtpFromCache(actionId);
    }

    @Transactional
    public Otp createOtp(UUID actionId, String receiverEmail, UUID userId, String action)
            throws Exception {
        if (isOtpExists(actionId)) {
            throw new CustomError("OTP Already Exists", HttpStatus.BAD_REQUEST);
        }

        Integer generatedOtp = generateOTP();
        Integer attemptsAllowed = Constants.getOtpAttempts(action);

        Otp otp = new Otp(actionId, action, generatedOtp, receiverEmail, userId, attemptsAllowed);

        Otp savedOtp = otpRepository.save(otp);

        updateCache(actionId, savedOtp);

        return savedOtp;
    }

    @Transactional
    public Otp resendOtp(UUID actionId) throws Exception {
        Otp otp = getOtpFromCache(actionId);

        if (OtpStatusEnum.OTP_VERIFIED.equals(otp.getStatus())) {
            throw new CustomError("OTP already verified. Resend is not allowed",
                    HttpStatus.BAD_REQUEST);
        }

        otp.setOtp(generateOTP());
        otp.setIncorrectAttempts(0);
        otp.setStatus(OtpStatusEnum.OTP_CREATED);

        Otp updatedOtp = otpRepository.save(otp);
        updateCache(actionId, updatedOtp);
        return updatedOtp;
    }

    public boolean checkOtp(UUID actionId, Integer enteredOtp) throws Exception {
        isAllowed(actionId);

        Otp otp = getOtpFromCache(actionId);

        if (Objects.equals(otp.getOtp(), enteredOtp)) {
            updateOtpStatus(actionId, OtpStatusEnum.OTP_VERIFIED);
            return true;
        }

        incrementOtpIncorrectAttempts(actionId);
        return false;
    }

    private Otp findOtpFromRepository(UUID actionId) throws Exception {
        return otpRepository.findByActionId(actionId)
                .orElseThrow(() -> new CustomError("OTP not found", HttpStatus.NOT_FOUND));
    }

    private boolean isOtpExists(UUID actionId) {
        return otpCache.containsKey(actionId) || otpRepository.findByActionId(actionId).isPresent();
    }


    // TODO need to write the destory cache alone
}
