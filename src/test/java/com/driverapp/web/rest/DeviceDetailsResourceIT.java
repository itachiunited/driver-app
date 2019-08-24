package com.driverapp.web.rest;

import com.driverapp.DriverApp;
import com.driverapp.domain.DeviceDetails;
import com.driverapp.repository.DeviceDetailsRepository;
import com.driverapp.repository.search.DeviceDetailsSearchRepository;
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

/**
 * Integration tests for the {@link DeviceDetailsResource} REST controller.
 */
@SpringBootTest(classes = DriverApp.class)
public class DeviceDetailsResourceIT {

    private static final String DEFAULT_DEVICE_ID = "AAAAAAAAAA";
    private static final String UPDATED_DEVICE_ID = "BBBBBBBBBB";

    @Autowired
    private DeviceDetailsRepository deviceDetailsRepository;

    /**
     * This repository is mocked in the com.driverapp.repository.search test package.
     *
     * @see com.driverapp.repository.search.DeviceDetailsSearchRepositoryMockConfiguration
     */
    @Autowired
    private DeviceDetailsSearchRepository mockDeviceDetailsSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restDeviceDetailsMockMvc;

    private DeviceDetails deviceDetails;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DeviceDetailsResource deviceDetailsResource = new DeviceDetailsResource(deviceDetailsRepository, mockDeviceDetailsSearchRepository);
        this.restDeviceDetailsMockMvc = MockMvcBuilders.standaloneSetup(deviceDetailsResource)
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
    public static DeviceDetails createEntity() {
        DeviceDetails deviceDetails = new DeviceDetails()
            .deviceId(DEFAULT_DEVICE_ID);
        return deviceDetails;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeviceDetails createUpdatedEntity() {
        DeviceDetails deviceDetails = new DeviceDetails()
            .deviceId(UPDATED_DEVICE_ID);
        return deviceDetails;
    }

    @BeforeEach
    public void initTest() {
        deviceDetailsRepository.deleteAll();
        deviceDetails = createEntity();
    }

    @Test
    public void createDeviceDetails() throws Exception {
        int databaseSizeBeforeCreate = deviceDetailsRepository.findAll().size();

        // Create the DeviceDetails
        restDeviceDetailsMockMvc.perform(post("/api/device-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDetails)))
            .andExpect(status().isCreated());

        // Validate the DeviceDetails in the database
        List<DeviceDetails> deviceDetailsList = deviceDetailsRepository.findAll();
        assertThat(deviceDetailsList).hasSize(databaseSizeBeforeCreate + 1);
        DeviceDetails testDeviceDetails = deviceDetailsList.get(deviceDetailsList.size() - 1);
        assertThat(testDeviceDetails.getDeviceId()).isEqualTo(DEFAULT_DEVICE_ID);

        // Validate the DeviceDetails in Elasticsearch
        verify(mockDeviceDetailsSearchRepository, times(1)).save(testDeviceDetails);
    }

    @Test
    public void createDeviceDetailsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = deviceDetailsRepository.findAll().size();

        // Create the DeviceDetails with an existing ID
        deviceDetails.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceDetailsMockMvc.perform(post("/api/device-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDetails)))
            .andExpect(status().isBadRequest());

        // Validate the DeviceDetails in the database
        List<DeviceDetails> deviceDetailsList = deviceDetailsRepository.findAll();
        assertThat(deviceDetailsList).hasSize(databaseSizeBeforeCreate);

        // Validate the DeviceDetails in Elasticsearch
        verify(mockDeviceDetailsSearchRepository, times(0)).save(deviceDetails);
    }


    @Test
    public void checkDeviceIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = deviceDetailsRepository.findAll().size();
        // set the field null
        deviceDetails.setDeviceId(null);

        // Create the DeviceDetails, which fails.

        restDeviceDetailsMockMvc.perform(post("/api/device-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDetails)))
            .andExpect(status().isBadRequest());

        List<DeviceDetails> deviceDetailsList = deviceDetailsRepository.findAll();
        assertThat(deviceDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllDeviceDetails() throws Exception {
        // Initialize the database
        deviceDetailsRepository.save(deviceDetails);

        // Get all the deviceDetailsList
        restDeviceDetailsMockMvc.perform(get("/api/device-details?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deviceDetails.getId())))
            .andExpect(jsonPath("$.[*].deviceId").value(hasItem(DEFAULT_DEVICE_ID.toString())));
    }
    
    @Test
    public void getDeviceDetails() throws Exception {
        // Initialize the database
        deviceDetailsRepository.save(deviceDetails);

        // Get the deviceDetails
        restDeviceDetailsMockMvc.perform(get("/api/device-details/{id}", deviceDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(deviceDetails.getId()))
            .andExpect(jsonPath("$.deviceId").value(DEFAULT_DEVICE_ID.toString()));
    }

    @Test
    public void getNonExistingDeviceDetails() throws Exception {
        // Get the deviceDetails
        restDeviceDetailsMockMvc.perform(get("/api/device-details/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateDeviceDetails() throws Exception {
        // Initialize the database
        deviceDetailsRepository.save(deviceDetails);

        int databaseSizeBeforeUpdate = deviceDetailsRepository.findAll().size();

        // Update the deviceDetails
        DeviceDetails updatedDeviceDetails = deviceDetailsRepository.findById(deviceDetails.getId()).get();
        updatedDeviceDetails
            .deviceId(UPDATED_DEVICE_ID);

        restDeviceDetailsMockMvc.perform(put("/api/device-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedDeviceDetails)))
            .andExpect(status().isOk());

        // Validate the DeviceDetails in the database
        List<DeviceDetails> deviceDetailsList = deviceDetailsRepository.findAll();
        assertThat(deviceDetailsList).hasSize(databaseSizeBeforeUpdate);
        DeviceDetails testDeviceDetails = deviceDetailsList.get(deviceDetailsList.size() - 1);
        assertThat(testDeviceDetails.getDeviceId()).isEqualTo(UPDATED_DEVICE_ID);

        // Validate the DeviceDetails in Elasticsearch
        verify(mockDeviceDetailsSearchRepository, times(1)).save(testDeviceDetails);
    }

    @Test
    public void updateNonExistingDeviceDetails() throws Exception {
        int databaseSizeBeforeUpdate = deviceDetailsRepository.findAll().size();

        // Create the DeviceDetails

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeviceDetailsMockMvc.perform(put("/api/device-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(deviceDetails)))
            .andExpect(status().isBadRequest());

        // Validate the DeviceDetails in the database
        List<DeviceDetails> deviceDetailsList = deviceDetailsRepository.findAll();
        assertThat(deviceDetailsList).hasSize(databaseSizeBeforeUpdate);

        // Validate the DeviceDetails in Elasticsearch
        verify(mockDeviceDetailsSearchRepository, times(0)).save(deviceDetails);
    }

    @Test
    public void deleteDeviceDetails() throws Exception {
        // Initialize the database
        deviceDetailsRepository.save(deviceDetails);

        int databaseSizeBeforeDelete = deviceDetailsRepository.findAll().size();

        // Delete the deviceDetails
        restDeviceDetailsMockMvc.perform(delete("/api/device-details/{id}", deviceDetails.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DeviceDetails> deviceDetailsList = deviceDetailsRepository.findAll();
        assertThat(deviceDetailsList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the DeviceDetails in Elasticsearch
        verify(mockDeviceDetailsSearchRepository, times(1)).deleteById(deviceDetails.getId());
    }

    @Test
    public void searchDeviceDetails() throws Exception {
        // Initialize the database
        deviceDetailsRepository.save(deviceDetails);
        when(mockDeviceDetailsSearchRepository.search(queryStringQuery("id:" + deviceDetails.getId())))
            .thenReturn(Collections.singletonList(deviceDetails));
        // Search the deviceDetails
        restDeviceDetailsMockMvc.perform(get("/api/_search/device-details?query=id:" + deviceDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deviceDetails.getId())))
            .andExpect(jsonPath("$.[*].deviceId").value(hasItem(DEFAULT_DEVICE_ID)));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeviceDetails.class);
        DeviceDetails deviceDetails1 = new DeviceDetails();
        deviceDetails1.setId("id1");
        DeviceDetails deviceDetails2 = new DeviceDetails();
        deviceDetails2.setId(deviceDetails1.getId());
        assertThat(deviceDetails1).isEqualTo(deviceDetails2);
        deviceDetails2.setId("id2");
        assertThat(deviceDetails1).isNotEqualTo(deviceDetails2);
        deviceDetails1.setId(null);
        assertThat(deviceDetails1).isNotEqualTo(deviceDetails2);
    }
}
