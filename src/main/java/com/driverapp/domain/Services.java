package com.driverapp.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;

import com.driverapp.domain.enumeration.ServiceType;

/**
 * A Services.
 */
@Document(collection = "services")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "services")
public class Services implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private String id;

    @Field("type")
    private ServiceType type;

    @DBRef
    @Field("driver")
    @JsonIgnoreProperties("services")
    private Driver driver;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ServiceType getType() {
        return type;
    }

    public Services type(ServiceType type) {
        this.type = type;
        return this;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public Driver getDriver() {
        return driver;
    }

    public Services driver(Driver driver) {
        this.driver = driver;
        return this;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Services)) {
            return false;
        }
        return id != null && id.equals(((Services) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Services{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            "}";
    }
}
