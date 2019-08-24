package com.driverapp.repository.search;

import com.driverapp.domain.Vehicle;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Vehicle} entity.
 */
public interface VehicleSearchRepository extends ElasticsearchRepository<Vehicle, String> {
}
