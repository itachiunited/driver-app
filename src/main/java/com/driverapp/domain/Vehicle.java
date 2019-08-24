package com.driverapp.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.time.Instant;

import com.driverapp.domain.enumeration.VehicleType;

/**
 * A Vehicle.
 */
@Document(collection = "vehicle")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "vehicle")
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private String id;

    @NotNull
    @Field("type")
    private VehicleType type;

    @NotNull
    @Field("make")
    private String make;

    @NotNull
    @Field("model")
    private String model;

    @NotNull
    @Field("year")
    private String year;

    @NotNull
    @Field("plate_number")
    private String plateNumber;

    @Field("support_heavy_transport")
    private Boolean supportHeavyTransport;

    @Field("vin_number")
    private String vinNumber;

    @NotNull
    @Field("registration_exp_date")
    private Instant registrationExpDate;

    @DBRef
    @Field("driver")
    @JsonIgnoreProperties("vehicles")
    private Driver driver;

    @DBRef
    @Field("autoInsurance")
    private Insurance autoInsurance;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VehicleType getType() {
        return type;
    }

    public Vehicle type(VehicleType type) {
        this.type = type;
        return this;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getMake() {
        return make;
    }

    public Vehicle make(String make) {
        this.make = make;
        return this;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public Vehicle model(String model) {
        this.model = model;
        return this;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public Vehicle year(String year) {
        this.year = year;
        return this;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public Vehicle plateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
        return this;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Boolean isSupportHeavyTransport() {
        return supportHeavyTransport;
    }

    public Vehicle supportHeavyTransport(Boolean supportHeavyTransport) {
        this.supportHeavyTransport = supportHeavyTransport;
        return this;
    }

    public void setSupportHeavyTransport(Boolean supportHeavyTransport) {
        this.supportHeavyTransport = supportHeavyTransport;
    }

    public String getVinNumber() {
        return vinNumber;
    }

    public Vehicle vinNumber(String vinNumber) {
        this.vinNumber = vinNumber;
        return this;
    }

    public void setVinNumber(String vinNumber) {
        this.vinNumber = vinNumber;
    }

    public Instant getRegistrationExpDate() {
        return registrationExpDate;
    }

    public Vehicle registrationExpDate(Instant registrationExpDate) {
        this.registrationExpDate = registrationExpDate;
        return this;
    }

    public void setRegistrationExpDate(Instant registrationExpDate) {
        this.registrationExpDate = registrationExpDate;
    }

    public Driver getDriver() {
        return driver;
    }

    public Vehicle driver(Driver driver) {
        this.driver = driver;
        return this;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Insurance getAutoInsurance() {
        return autoInsurance;
    }

    public Vehicle autoInsurance(Insurance insurance) {
        this.autoInsurance = insurance;
        return this;
    }

    public void setAutoInsurance(Insurance insurance) {
        this.autoInsurance = insurance;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle)) {
            return false;
        }
        return id != null && id.equals(((Vehicle) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", make='" + getMake() + "'" +
            ", model='" + getModel() + "'" +
            ", year='" + getYear() + "'" +
            ", plateNumber='" + getPlateNumber() + "'" +
            ", supportHeavyTransport='" + isSupportHeavyTransport() + "'" +
            ", vinNumber='" + getVinNumber() + "'" +
            ", registrationExpDate='" + getRegistrationExpDate() + "'" +
            "}";
    }
}
