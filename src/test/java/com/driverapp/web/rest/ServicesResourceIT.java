package com.driverapp.web.rest;

import com.driverapp.DriverApp;
import com.driverapp.domain.Services;
import com.driverapp.repository.ServicesRepository;
import com.driverapp.repository.search.ServicesSearchRepository;
import com.driverapp.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;


import java.util.Collections;
import java.util.List;

import static com.driverapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.driverapp.domain.enumeration.ServiceType;
/**
 * Integration tests for the {@link ServicesResource} REST controller.
 */
@SpringBootTest(classes = DriverApp.class)
public class ServicesResourceIT {

    private static final ServiceType DEFAULT_TYPE = ServiceType.FoodDelivery;
    private static final ServiceType UPDATED_TYPE = ServiceType.Errands;

    @Autowired
    private ServicesRepository servicesRepository;

    /**
     * This repository is mocked in the com.driverapp.repository.search test package.
     *
     * @see com.driverapp.repository.search.ServicesSearchRepositoryMockConfiguration
     */
    @Autowired
    private ServicesSearchRepository mockServicesSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restServicesMockMvc;

    private Services services;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ServicesResource servicesResource = new ServicesResource(servicesRepository, mockServicesSearchRepository);
        this.restServicesMockMvc = MockMvcBuilders.standaloneSetup(servicesResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Services createEntity() {
        Services services = new Services()
            .type(DEFAULT_TYPE);
        return services;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Services createUpdatedEntity() {
        Services services = new Services()
            .type(UPDATED_TYPE);
        return services;
    }

    @BeforeEach
    public void initTest() {
        servicesRepository.deleteAll();
        services = createEntity();
    }

    @Test
    public void createServices() throws Exception {
        int databaseSizeBeforeCreate = servicesRepository.findAll().size();

        // Create the Services
        restServicesMockMvc.perform(post("/api/services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(services)))
            .andExpect(status().isCreated());

        // Validate the Services in the database
        List<Services> servicesList = servicesRepository.findAll();
        assertThat(servicesList).hasSize(databaseSizeBeforeCreate + 1);
        Services testServices = servicesList.get(servicesList.size() - 1);
        assertThat(testServices.getType()).isEqualTo(DEFAULT_TYPE);

        // Validate the Services in Elasticsearch
        verify(mockServicesSearchRepository, times(1)).save(testServices);
    }

    @Test
    public void createServicesWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = servicesRepository.findAll().size();

        // Create the Services with an existing ID
        services.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restServicesMockMvc.perform(post("/api/services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(services)))
            .andExpect(status().isBadRequest());

        // Validate the Services in the database
        List<Services> servicesList = servicesRepository.findAll();
        assertThat(servicesList).hasSize(databaseSizeBeforeCreate);

        // Validate the Services in Elasticsearch
        verify(mockServicesSearchRepository, times(0)).save(services);
    }


    @Test
    public void getAllServices() throws Exception {
        // Initialize the database
        servicesRepository.save(services);

        // Get all the servicesList
        restServicesMockMvc.perform(get("/api/services?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(services.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }
    
    @Test
    public void getServices() throws Exception {
        // Initialize the database
        servicesRepository.save(services);

        // Get the services
        restServicesMockMvc.perform(get("/api/services/{id}", services.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(services.getId()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    public void getNonExistingServices() throws Exception {
        // Get the services
        restServicesMockMvc.perform(get("/api/services/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateServices() throws Exception {
        // Initialize the database
        servicesRepository.save(services);

        int databaseSizeBeforeUpdate = servicesRepository.findAll().size();

        // Update the services
        Services updatedServices = servicesRepository.findById(services.getId()).get();
        updatedServices
            .type(UPDATED_TYPE);

        restServicesMockMvc.perform(put("/api/services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedServices)))
            .andExpect(status().isOk());

        // Validate the Services in the database
        List<Services> servicesList = servicesRepository.findAll();
        assertThat(servicesList).hasSize(databaseSizeBeforeUpdate);
        Services testServices = servicesList.get(servicesList.size() - 1);
        assertThat(testServices.getType()).isEqualTo(UPDATED_TYPE);

        // Validate the Services in Elasticsearch
        verify(mockServicesSearchRepository, times(1)).save(testServices);
    }

    @Test
    public void updateNonExistingServices() throws Exception {
        int databaseSizeBeforeUpdate = servicesRepository.findAll().size();

        // Create the Services

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restServicesMockMvc.perform(put("/api/services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(services)))
            .andExpect(status().isBadRequest());

        // Validate the Services in the database
        List<Services> servicesList = servicesRepository.findAll();
        assertThat(servicesList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Services in Elasticsearch
        verify(mockServicesSearchRepository, times(0)).save(services);
    }

    @Test
    public void deleteServices() throws Exception {
        // Initialize the database
        servicesRepository.save(services);

        int databaseSizeBeforeDelete = servicesRepository.findAll().size();

        // Delete the services
        restServicesMockMvc.perform(delete("/api/services/{id}", services.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Services> servicesList = servicesRepository.findAll();
        assertThat(servicesList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Services in Elasticsearch
        verify(mockServicesSearchRepository, times(1)).deleteById(services.getId());
    }

    @Test
    public void searchServices() throws Exception {
        // Initialize the database
        servicesRepository.save(services);
        when(mockServicesSearchRepository.search(queryStringQuery("id:" + services.getId())))
            .thenReturn(Collections.singletonList(services));
        // Search the services
        restServicesMockMvc.perform(get("/api/_search/services?query=id:" + services.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(services.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Services.class);
        Services services1 = new Services();
        services1.setId("id1");
        Services services2 = new Services();
        services2.setId(services1.getId());
        assertThat(services1).isEqualTo(services2);
        services2.setId("id2");
        assertThat(services1).isNotEqualTo(services2);
        services1.setId(null);
        assertThat(services1).isNotEqualTo(services2);
    }
}
