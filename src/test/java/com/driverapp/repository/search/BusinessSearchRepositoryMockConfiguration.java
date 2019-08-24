package com.driverapp.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link BusinessSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class BusinessSearchRepositoryMockConfiguration {

    @MockBean
    private BusinessSearchRepository mockBusinessSearchRepository;

}
