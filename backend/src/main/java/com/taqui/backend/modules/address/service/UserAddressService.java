package com.taqui.backend.modules.address.service;

import com.taqui.backend.modules.address.entity.UserAddress;
import com.taqui.backend.modules.address.exception.AddressNotFoundException;
import com.taqui.backend.modules.address.repository.UserAddressRepository;
import com.taqui.backend.modules.order.dto.AddressDTO;
import com.taqui.backend.modules.order.entity.Address;
import com.taqui.backend.modules.user.entity.User;
import com.taqui.backend.modules.user.exception.UserNotFoundException;
import com.taqui.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserAddress> findMyAddresses(UUID ownerId) {
        return userAddressRepository.findByOwner_UserIdOrderByCreatedAtDesc(ownerId);
    }

    @Transactional
    public UserAddress createAddress(AddressDTO dto, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        UserAddress entity = new UserAddress();
        entity.setOwner(owner);
        entity.setAddress(toAddress(dto));
        return userAddressRepository.save(entity);
    }

    @Transactional
    public void deleteAddress(UUID addressId, UUID ownerId) {
        UserAddress entity = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Endereço não encontrado: " + addressId));
        if (!entity.getOwner().getUserId().equals(ownerId)) {
            throw new AccessDeniedException("Este endereço não é seu");
        }
        userAddressRepository.delete(entity);
    }

    private Address toAddress(AddressDTO dto) {
        Address address = new Address();
        address.setRecipientName(dto.recipientName());
        address.setPostalCode(dto.postalCode());
        address.setStreet(dto.street());
        address.setNumber(dto.number());
        address.setComplement(dto.complement());
        address.setDistrict(dto.district());
        address.setCity(dto.city());
        address.setState(dto.state());
        return address;
    }
}
