package ee.adit.dhx.api.container.v2_1;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class PostalAddressType {
    private String country;
    private String county;
    private String localGovernment;
    private String administrativeUnit;
    private String smallPlace;
    private String landUnit;
    private String street;
    private String houseNumber;
    private String buildingPartNumber;
    private String postalCode;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getLocalGovernment() {
        return localGovernment;
    }

    public void setLocalGovernment(String localGovernment) {
        this.localGovernment = localGovernment;
    }

    public String getAdministrativeUnit() {
        return administrativeUnit;
    }

    public void setAdministrativeUnit(String administrativeUnit) {
        this.administrativeUnit = administrativeUnit;
    }

    public String getSmallPlace() {
        return smallPlace;
    }

    public void setSmallPlace(String smallPlace) {
        this.smallPlace = smallPlace;
    }

    public String getLandUnit() {
        return landUnit;
    }

    public void setLandUnit(String landUnit) {
        this.landUnit = landUnit;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getBuildingPartNumber() {
        return buildingPartNumber;
    }

    public void setBuildingPartNumber(String buildingPartNumber) {
        this.buildingPartNumber = buildingPartNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
