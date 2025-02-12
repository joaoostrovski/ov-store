package ostro.veda.service;

import ostro.veda.common.InputValidator;
import ostro.veda.common.ProcessDataType;
import ostro.veda.common.dto.AddressDTO;
import ostro.veda.db.AddressRepository;

public class AddressService {

    AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressDTO processData(String streetAddress, String addressNumber, String addressType, String city,
                                         String state, String zip_code, String country, boolean isActive, ProcessDataType dmlType) {

        try {
            if (dmlType == null) {
                return null;
            }

            // implement Google Map API latter
//        int minimumTemporaryLength = 3;

            String streetAddressCheck = InputValidator.stringChecker(streetAddress, false, true, false, 1);
            String addressNumberCheck = InputValidator.stringChecker(addressNumber, false, true, false, 1);
            String addressTypeCheck = InputValidator.stringChecker(addressType, false, true, false, 1);
            String cityCheck = InputValidator.stringChecker(city, false, true, false, 1);
            String stateCheck = InputValidator.stringChecker(state, false, true, false, 1);
            String zip_codeCheck = InputValidator.stringChecker(zip_code, false, true, false, 1);
            String countryCheck = InputValidator.stringChecker(country, false, true, false, 1);

            if (streetAddressCheck == null || addressNumberCheck == null || addressTypeCheck == null ||
                    cityCheck == null || stateCheck == null || zip_codeCheck == null || countryCheck == null) {
                return null;
            }

            return performDmlAction(streetAddress, addressNumber, addressType, city,
                    state, zip_code, country, isActive, dmlType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private AddressDTO performDmlAction(String streetAddress, String addressNumber, String addressType, String city,
                                        String state, String zip_code, String country, boolean isActive, ProcessDataType dmlType) {
        switch (dmlType) {
            case ADD -> {
                AddressDTO addressDTO = this.addressRepository.addAddress(streetAddress, addressNumber, addressType, city, state, zip_code, country, isActive);
                this.addressRepository.closeEm();
                return addressDTO;
            }
            default -> {
                return null;
            }
        }
    }
}
