package com.driverapp.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.driverapp.domain.enumeration.Status;

import com.driverapp.domain.enumeration.PhoneType;

/**
 * A Driver.
 */
@Document(collection = "driver")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "driver")
public class Driver implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private String id;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @NotNull
    @Field("phone_number")
    private String phoneNumber;

    @Field("status")
    private Status status;

    @Field("one_time_code")
    private String oneTimeCode;

    @Field("one_time_expiration_time")
    private Instant oneTimeExpirationTime;

    @Field("driver_license")
    private String driverLicense;

    @Field("phone_type")
    private PhoneType phoneType;

    @Field("dui_conviction")
    private Boolean duiConviction;

    @Field("felony_conviction")
    private Boolean felonyConviction;

    @DBRef
    @Field("location")
    private Location location;

    @DBRef
    @Field("car")
    private Set<Vehicle> cars = new HashSet<>();

    @DBRef
    @Field("services")
    private Set<Services> services = new HashSet<>();

    @DBRef
    @Field("devices")
    private Set<DeviceDetails> devices = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public Driver firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Driver lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public Driver email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public Driver password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Driver phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Status getStatus() {
        return status;
    }

    public Driver status(Status status) {
        this.status = status;
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOneTimeCode() {
        return oneTimeCode;
    }

    public Driver oneTimeCode(String oneTimeCode) {
        this.oneTimeCode = oneTimeCode;
        return this;
    }

    public void setOneTimeCode(String oneTimeCode) {
        this.oneTimeCode = oneTimeCode;
    }

    public Instant getOneTimeExpirationTime() {
        return oneTimeExpirationTime;
    }

    public Driver oneTimeExpirationTime(Instant oneTimeExpirationTime) {
        this.oneTimeExpirationTime = oneTimeExpirationTime;
        return this;
    }

    public void setOneTimeExpirationTime(Instant oneTimeExpirationTime) {
        this.oneTimeExpirationTime = oneTimeExpirationTime;
    }

    public String getDriverLicense() {
        return driverLicense;
    }

    public Driver driverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
        return this;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public PhoneType getPhoneType() {
        return phoneType;
    }

    public Driver phoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
        return this;
    }

    public void setPhoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
    }

    public Boolean isDuiConviction() {
        return duiConviction;
    }

    public Driver duiConviction(Boolean duiConviction) {
        this.duiConviction = duiConviction;
        return this;
    }

    public void setDuiConviction(Boolean duiConviction) {
        this.duiConviction = duiConviction;
    }

    public Boolean isFelonyConviction() {
        return felonyConviction;
    }

    public Driver felonyConviction(Boolean felonyConviction) {
        this.felonyConviction = felonyConviction;
        return this;
    }

    public void setFelonyConviction(Boolean felonyConviction) {
        this.felonyConviction = felonyConviction;
    }

    public Location getLocation() {
        return location;
    }

    public Driver location(Location location) {
        this.location = location;
        return this;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<Vehicle> getCars() {
        return cars;
    }

    public Driver cars(Set<Vehicle> vehicles) {
        this.cars = vehicles;
        return this;
    }

    public Driver addCar(Vehicle vehicle) {
        this.cars.add(vehicle);
        vehicle.setDriver(this);
        return this;
    }

    public Driver removeCar(Vehicle vehicle) {
        this.cars.remove(vehicle);
        vehicle.setDriver(null);
        return this;
    }

    public void setCars(Set<Vehicle> vehicles) {
        this.cars = vehicles;
    }

    public Set<Services> getServices() {
        return services;
    }

    public Driver services(Set<Services> services) {
        this.services = services;
        return this;
    }

    public Driver addServices(Services services) {
        this.services.add(services);
        services.setDriver(this);
        return this;
    }

    public Driver removeServices(Services services) {
        this.services.remove(services);
        services.setDriver(null);
        return this;
    }

    public void setServices(Set<Services> services) {
        this.services = services;
    }

    public Set<DeviceDetails> getDevices() {
        return devices;
    }

    public Driver devices(Set<DeviceDetails> deviceDetails) {
        this.devices = deviceDetails;
        return this;
    }

    public Driver addDevices(DeviceDetails deviceDetails) {
        this.devices.add(deviceDetails);
        deviceDetails.setDriver(this);
        return this;
    }

    public Driver removeDevices(DeviceDetails deviceDetails) {
        this.devices.remove(deviceDetails);
        deviceDetails.setDriver(null);
        return this;
    }

    public void setDevices(Set<DeviceDetails> deviceDetails) {
        this.devices = deviceDetails;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Driver)) {
            return false;
        }
        return id != null && id.equals(((Driver) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Driver{" +
            "id=" + getId() +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", email='" + getEmail() + "'" +
            ", password='" + getPassword() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", status='" + getStatus() + "'" +
            ", oneTimeCode='" + getOneTimeCode() + "'" +
            ", oneTimeExpirationTime='" + getOneTimeExpirationTime() + "'" +
            ", driverLicense='" + getDriverLicense() + "'" +
            ", phoneType='" + getPhoneType() + "'" +
            ", duiConviction='" + isDuiConviction() + "'" +
            ", felonyConviction='" + isFelonyConviction() + "'" +
            "}";
    }
}
