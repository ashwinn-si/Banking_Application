package com.ashwinsi.bankingApplication.Repository;

import com.ashwinsi.bankingApplication.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    public Optional<User> findByEmailAndIsActivated(String email, boolean isActivated);

    public Optional<User> findByPhoneNumberAndIsActivated(String phoneNumber, boolean isActivated);

}
