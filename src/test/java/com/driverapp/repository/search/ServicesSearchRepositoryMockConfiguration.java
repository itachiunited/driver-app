package com.driverapp.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link ServicesSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class ServicesSearchRepositoryMockConfiguration {

    @MockBean
    private ServicesSearchRepository mockServicesSearchRepository;

}
