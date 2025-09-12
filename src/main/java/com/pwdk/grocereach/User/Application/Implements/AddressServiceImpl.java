package com.pwdk.grocereach.User.Application.Implements; // Or your correct implementation package

import com.pwdk.grocereach.Auth.Domain.Entities.User;
import com.pwdk.grocereach.Auth.Infrastructure.Repositories.UserRepository;
import com.pwdk.grocereach.User.Application.Services.AddressService;
import com.pwdk.grocereach.User.Domain.Entities.Address;
import com.pwdk.grocereach.User.Infrastructure.Repositories.AddressRepository;
import com.pwdk.grocereach.User.Presentation.Dto.AddressRequest;
import com.pwdk.grocereach.User.Presentation.Dto.AddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getUserAddresses(String userEmail) {
        User user = findUserByEmail(userEmail);
        return addressRepository.findByUser(user).stream()
                .map(AddressResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse createAddress(String userEmail, AddressRequest request) {
        User user = findUserByEmail(userEmail);

        if (request.isPrimary()) {
            unsetOtherPrimaryAddresses(user);
        }

        Address newAddress = new Address();
        mapRequestToEntity(request, newAddress);
        newAddress.setUser(user);

        Address savedAddress = addressRepository.save(newAddress);
        return new AddressResponse(savedAddress);
    }

    @Override
    public AddressResponse updateAddress(String userEmail, UUID addressId, AddressRequest request) {
        User user = findUserByEmail(userEmail);
        Address address = findAddressByIdAndUser(addressId, user);

        if (request.isPrimary() && !address.isPrimary()) {
            unsetOtherPrimaryAddresses(user);
        }

        mapRequestToEntity(request, address);
        Address updatedAddress = addressRepository.save(address);
        return new AddressResponse(updatedAddress);
    }

    @Override
    public void deleteAddress(String userEmail, UUID addressId) {
        User user = findUserByEmail(userEmail);
        Address address = findAddressByIdAndUser(addressId, user);
        addressRepository.delete(address);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Address findAddressByIdAndUser(UUID addressId, User user) {
        return addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new RuntimeException("Address not found or does not belong to user"));
    }

    private void unsetOtherPrimaryAddresses(User user) {
        addressRepository.findByUser(user).forEach(address -> {
            if (address.isPrimary()) {
                address.setPrimary(false);
                addressRepository.save(address);
            }
        });
    }

    private void mapRequestToEntity(AddressRequest request, Address address) {
        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhone(request.getPhone());
        address.setFullAddress(request.getFullAddress());
<<<<<<< Updated upstream
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
=======
        address.setProvince(province);
        address.setCity(city);
>>>>>>> Stashed changes
        address.setPostalCode(request.getPostalCode());
        address.setPrimary(request.isPrimary());
    }
}