package com.driverapp.repository.search;

import com.driverapp.domain.Business;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Business} entity.
 */
public interface BusinessSearchRepository extends ElasticsearchRepository<Business, String> {
}
