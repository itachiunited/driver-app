package com.driverapp.repository.search;

import com.driverapp.domain.Services;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Services} entity.
 */
public interface ServicesSearchRepository extends ElasticsearchRepository<Services, String> {
}
