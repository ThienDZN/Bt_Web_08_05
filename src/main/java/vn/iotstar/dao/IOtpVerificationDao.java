package vn.iotstar.dao;

import vn.iotstar.entity.OtpVerification;

public interface IOtpVerificationDao {
    void insert(OtpVerification otpVerification);
    void markAllUnusedAsUsed(String email, String purpose);
    OtpVerification findLatest(String email, String purpose);
    void update(OtpVerification otpVerification);
}
