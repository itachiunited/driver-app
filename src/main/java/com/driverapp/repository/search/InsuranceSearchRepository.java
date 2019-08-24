package com.driverapp.repository.search;

import com.driverapp.domain.Insurance;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Insurance} entity.
 */
public interface InsuranceSearchRepository extends ElasticsearchRepository<Insurance, String> {
}
