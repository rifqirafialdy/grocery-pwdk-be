package com.pwdk.grocereach.User.Presentation.Dto;

import com.pwdk.grocereach.User.Domain.Entities.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AddressResponse {
    private UUID id;
    private String label;
    private String recipientName;
    private String phone;
    private String fullAddress;
    private String postalCode;
    private boolean isPrimary;
    private Integer provinceId;
    private String province;
    private Integer cityId;
    private String city;

    public AddressResponse(Address address) {
        this.id = address.getId();
        this.label = address.getLabel();
        this.recipientName = address.getRecipientName();
        this.phone = address.getPhone();
        this.fullAddress = address.getFullAddress();
<<<<<<< Updated upstream
        this.city = address.getCity();
        this.province = address.getProvince();
=======
>>>>>>> Stashed changes
        this.postalCode = address.getPostalCode();
        this.isPrimary = address.isPrimary();

        if (address.getProvince() != null) {
            this.provinceId = address.getProvince().getId();
            this.province = address.getProvince().getName();
        }
        if (address.getCity() != null) {
            this.cityId = address.getCity().getId();
            this.city = address.getCity().getName();
        }
    }
}
