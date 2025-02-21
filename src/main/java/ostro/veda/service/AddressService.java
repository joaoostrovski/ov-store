package ostro.veda.service;

import ostro.veda.common.InputValidator;
import ostro.veda.common.ProcessDataType;
import ostro.veda.common.dto.AddressDTO;
import ostro.veda.common.error.ErrorHandling;
import ostro.veda.db.AddressRepository;
import ostro.veda.db.helpers.JPAUtil;
import ostro.veda.loggerService.Logger;

public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressDTO processData(int addressId, int userId, String streetAddress, String addressNumber, String addressType, String city,
                                  String state, String zipCode, String country, boolean isActive, ProcessDataType processDataType) {

        try {
            if (!hasValidInput(streetAddress, addressNumber, addressType, city, state, zipCode, country, processDataType))
                return null;
            streetAddress = InputValidator.stringSanitize(streetAddress);
            addressNumber = InputValidator.stringSanitize(addressNumber);
            city = InputValidator.stringSanitize(city);
            state = InputValidator.stringSanitize(state);
            country = InputValidator.stringSanitize(country);
            if (!hasValidLength(city, state, zipCode, country)) return null;

            return performDmlAction(addressId, userId, streetAddress, addressNumber, addressType, city,
                    state, zipCode, country, isActive, processDataType);
        } catch (Exception e) {
            Logger.log(e);
            return null;
        }
    }

    private boolean hasValidInput(String streetAddress, String addressNumber, String addressType, String city,
                                  String state, String zipCode, String country, ProcessDataType processDataType)
            throws ErrorHandling.InvalidStreetAddressException, ErrorHandling.InvalidAddressNumberException, ErrorHandling.InvalidAddressTypeException {
        // implement Google Map API (Geocoding)
        String cityCheck = InputValidator.stringChecker(city, false, true, false, 1);
        String stateCheck = InputValidator.stringChecker(state, false, true, false, 1);
        String zipCodeCheck = InputValidator.stringChecker(zipCode, false, true, false, 1);
        String countryCheck = InputValidator.stringChecker(country, false, true, false, 1);

        return InputValidator.hasValidStreetAddress(streetAddress) &&
                InputValidator.hasValidAddressNumber(addressNumber) &&
                InputValidator.hasValidAddressType(addressType) &&
                cityCheck != null &&
                stateCheck != null &&
                zipCodeCheck != null &&
                countryCheck != null &&
                processDataType != null;
    }

    public boolean hasValidLength(String city, String state, String zipCode, String country) throws ErrorHandling.InvalidLengthException {
        int standardMin = 1;
        int standardMax = 255;
        int numberTypeZipcodeMaxLength = 50;

        return InputValidator.hasValidLength(state, standardMin, standardMax) &&
                InputValidator.hasValidLength(country, standardMin, standardMax) &&
                InputValidator.hasValidLength(city, standardMin, standardMax) &&
                InputValidator.hasValidLength(zipCode, standardMin, numberTypeZipcodeMaxLength);
    }

    private AddressDTO performDmlAction(int addressId, int userId, String streetAddress, String addressNumber, String addressType, String city,
                                        String state, String zipCode, String country, boolean isActive, ProcessDataType processDataType) {
        switch (processDataType) {
            case ADD -> {
                AddressDTO addressDTO = this.addressRepository.addAddress(userId, streetAddress, addressNumber, addressType, city, state, zipCode, country, isActive);
                JPAUtil.closeEntityManager(this.addressRepository.getEm());
                return addressDTO;
            }
            case UPDATE -> {
                if (addressId < 1) {
                    return null;
                }
                AddressDTO addressDTO = this.addressRepository.updateAddress(addressId, userId, streetAddress, addressNumber, addressType, city, state, zipCode, country, isActive);
                JPAUtil.closeEntityManager(this.addressRepository.getEm());
                return addressDTO;
            }
            default -> {
                return null;
            }
        }
    }
}
