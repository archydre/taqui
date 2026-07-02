package com.taqui.backend.modules.address.repository;

import com.taqui.backend.modules.address.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByOwner_UserIdOrderByCreatedAtDesc(UUID ownerId);
}
