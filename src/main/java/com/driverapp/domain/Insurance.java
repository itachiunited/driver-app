package com.driverapp.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.time.Instant;

import com.driverapp.domain.enumeration.InsuranceType;

/**
 * A Insurance.
 */
@Document(collection = "insurance")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "insurance")
public class Insurance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @org.springframework.data.elasticsearch.annotations.Field(type = FieldType.Keyword)
    private String id;

    @NotNull
    @Field("type")
    private InsuranceType type;

    @NotNull
    @Field("insurance_provider")
    private String insuranceProvider;

    @NotNull
    @Field("insurance_exp_date")
    private Instant insuranceExpDate;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InsuranceType getType() {
        return type;
    }

    public Insurance type(InsuranceType type) {
        this.type = type;
        return this;
    }

    public void setType(InsuranceType type) {
        this.type = type;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public Insurance insuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
        return this;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public Instant getInsuranceExpDate() {
        return insuranceExpDate;
    }

    public Insurance insuranceExpDate(Instant insuranceExpDate) {
        this.insuranceExpDate = insuranceExpDate;
        return this;
    }

    public void setInsuranceExpDate(Instant insuranceExpDate) {
        this.insuranceExpDate = insuranceExpDate;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Insurance)) {
            return false;
        }
        return id != null && id.equals(((Insurance) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Insurance{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", insuranceProvider='" + getInsuranceProvider() + "'" +
            ", insuranceExpDate='" + getInsuranceExpDate() + "'" +
            "}";
    }
}
