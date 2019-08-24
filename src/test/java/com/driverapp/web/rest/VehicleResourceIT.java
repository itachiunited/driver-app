package com.driverapp.web.rest;

import com.driverapp.DriverApp;
import com.driverapp.domain.Vehicle;
import com.driverapp.repository.VehicleRepository;
import com.driverapp.repository.search.VehicleSearchRepository;
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


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static com.driverapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.driverapp.domain.enumeration.VehicleType;
/**
 * Integration tests for the {@link VehicleResource} REST controller.
 */
@SpringBootTest(classes = DriverApp.class)
public class VehicleResourceIT {

    private static final VehicleType DEFAULT_TYPE = VehicleType.SEDAN;
    private static final VehicleType UPDATED_TYPE = VehicleType.SUV;

    private static final String DEFAULT_MAKE = "AAAAAAAAAA";
    private static final String UPDATED_MAKE = "BBBBBBBBBB";

    private static final String DEFAULT_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_MODEL = "BBBBBBBBBB";

    private static final String DEFAULT_YEAR = "AAAAAAAAAA";
    private static final String UPDATED_YEAR = "BBBBBBBBBB";

    private static final String DEFAULT_PLATE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PLATE_NUMBER = "BBBBBBBBBB";

    private static final Boolean DEFAULT_SUPPORT_HEAVY_TRANSPORT = false;
    private static final Boolean UPDATED_SUPPORT_HEAVY_TRANSPORT = true;

    private static final String DEFAULT_VIN_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_VIN_NUMBER = "BBBBBBBBBB";

    private static final Instant DEFAULT_REGISTRATION_EXP_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REGISTRATION_EXP_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final Instant SMALLER_REGISTRATION_EXP_DATE = Instant.ofEpochMilli(-1L);

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * This repository is mocked in the com.driverapp.repository.search test package.
     *
     * @see com.driverapp.repository.search.VehicleSearchRepositoryMockConfiguration
     */
    @Autowired
    private VehicleSearchRepository mockVehicleSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restVehicleMockMvc;

    private Vehicle vehicle;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final VehicleResource vehicleResource = new VehicleResource(vehicleRepository, mockVehicleSearchRepository);
        this.restVehicleMockMvc = MockMvcBuilders.standaloneSetup(vehicleResource)
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
    public static Vehicle createEntity() {
        Vehicle vehicle = new Vehicle()
            .type(DEFAULT_TYPE)
            .make(DEFAULT_MAKE)
            .model(DEFAULT_MODEL)
            .year(DEFAULT_YEAR)
            .plateNumber(DEFAULT_PLATE_NUMBER)
            .supportHeavyTransport(DEFAULT_SUPPORT_HEAVY_TRANSPORT)
            .vinNumber(DEFAULT_VIN_NUMBER)
            .registrationExpDate(DEFAULT_REGISTRATION_EXP_DATE);
        return vehicle;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Vehicle createUpdatedEntity() {
        Vehicle vehicle = new Vehicle()
            .type(UPDATED_TYPE)
            .make(UPDATED_MAKE)
            .model(UPDATED_MODEL)
            .year(UPDATED_YEAR)
            .plateNumber(UPDATED_PLATE_NUMBER)
            .supportHeavyTransport(UPDATED_SUPPORT_HEAVY_TRANSPORT)
            .vinNumber(UPDATED_VIN_NUMBER)
            .registrationExpDate(UPDATED_REGISTRATION_EXP_DATE);
        return vehicle;
    }

    @BeforeEach
    public void initTest() {
        vehicleRepository.deleteAll();
        vehicle = createEntity();
    }

    @Test
    public void createVehicle() throws Exception {
        int databaseSizeBeforeCreate = vehicleRepository.findAll().size();

        // Create the Vehicle
        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isCreated());

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeCreate + 1);
        Vehicle testVehicle = vehicleList.get(vehicleList.size() - 1);
        assertThat(testVehicle.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testVehicle.getMake()).isEqualTo(DEFAULT_MAKE);
        assertThat(testVehicle.getModel()).isEqualTo(DEFAULT_MODEL);
        assertThat(testVehicle.getYear()).isEqualTo(DEFAULT_YEAR);
        assertThat(testVehicle.getPlateNumber()).isEqualTo(DEFAULT_PLATE_NUMBER);
        assertThat(testVehicle.isSupportHeavyTransport()).isEqualTo(DEFAULT_SUPPORT_HEAVY_TRANSPORT);
        assertThat(testVehicle.getVinNumber()).isEqualTo(DEFAULT_VIN_NUMBER);
        assertThat(testVehicle.getRegistrationExpDate()).isEqualTo(DEFAULT_REGISTRATION_EXP_DATE);

        // Validate the Vehicle in Elasticsearch
        verify(mockVehicleSearchRepository, times(1)).save(testVehicle);
    }

    @Test
    public void createVehicleWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = vehicleRepository.findAll().size();

        // Create the Vehicle with an existing ID
        vehicle.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeCreate);

        // Validate the Vehicle in Elasticsearch
        verify(mockVehicleSearchRepository, times(0)).save(vehicle);
    }


    @Test
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().size();
        // set the field null
        vehicle.setType(null);

        // Create the Vehicle, which fails.

        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkMakeIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().size();
        // set the field null
        vehicle.setMake(null);

        // Create the Vehicle, which fails.

        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkModelIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().size();
        // set the field null
        vehicle.setModel(null);

        // Create the Vehicle, which fails.

        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkYearIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().size();
        // set the field null
        vehicle.setYear(null);

        // Create the Vehicle, which fails.

        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkPlateNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().size();
        // set the field null
        vehicle.setPlateNumber(null);

        // Create the Vehicle, which fails.

        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkRegistrationExpDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = vehicleRepository.findAll().size();
        // set the field null
        vehicle.setRegistrationExpDate(null);

        // Create the Vehicle, which fails.

        restVehicleMockMvc.perform(post("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllVehicles() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle);

        // Get all the vehicleList
        restVehicleMockMvc.perform(get("/api/vehicles?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vehicle.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE.toString())))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL.toString())))
            .andExpect(jsonPath("$.[*].year").value(hasItem(DEFAULT_YEAR.toString())))
            .andExpect(jsonPath("$.[*].plateNumber").value(hasItem(DEFAULT_PLATE_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].supportHeavyTransport").value(hasItem(DEFAULT_SUPPORT_HEAVY_TRANSPORT.booleanValue())))
            .andExpect(jsonPath("$.[*].vinNumber").value(hasItem(DEFAULT_VIN_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].registrationExpDate").value(hasItem(DEFAULT_REGISTRATION_EXP_DATE.toString())));
    }
    
    @Test
    public void getVehicle() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle);

        // Get the vehicle
        restVehicleMockMvc.perform(get("/api/vehicles/{id}", vehicle.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(vehicle.getId()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.make").value(DEFAULT_MAKE.toString()))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL.toString()))
            .andExpect(jsonPath("$.year").value(DEFAULT_YEAR.toString()))
            .andExpect(jsonPath("$.plateNumber").value(DEFAULT_PLATE_NUMBER.toString()))
            .andExpect(jsonPath("$.supportHeavyTransport").value(DEFAULT_SUPPORT_HEAVY_TRANSPORT.booleanValue()))
            .andExpect(jsonPath("$.vinNumber").value(DEFAULT_VIN_NUMBER.toString()))
            .andExpect(jsonPath("$.registrationExpDate").value(DEFAULT_REGISTRATION_EXP_DATE.toString()));
    }

    @Test
    public void getNonExistingVehicle() throws Exception {
        // Get the vehicle
        restVehicleMockMvc.perform(get("/api/vehicles/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateVehicle() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle);

        int databaseSizeBeforeUpdate = vehicleRepository.findAll().size();

        // Update the vehicle
        Vehicle updatedVehicle = vehicleRepository.findById(vehicle.getId()).get();
        updatedVehicle
            .type(UPDATED_TYPE)
            .make(UPDATED_MAKE)
            .model(UPDATED_MODEL)
            .year(UPDATED_YEAR)
            .plateNumber(UPDATED_PLATE_NUMBER)
            .supportHeavyTransport(UPDATED_SUPPORT_HEAVY_TRANSPORT)
            .vinNumber(UPDATED_VIN_NUMBER)
            .registrationExpDate(UPDATED_REGISTRATION_EXP_DATE);

        restVehicleMockMvc.perform(put("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedVehicle)))
            .andExpect(status().isOk());

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);
        Vehicle testVehicle = vehicleList.get(vehicleList.size() - 1);
        assertThat(testVehicle.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testVehicle.getMake()).isEqualTo(UPDATED_MAKE);
        assertThat(testVehicle.getModel()).isEqualTo(UPDATED_MODEL);
        assertThat(testVehicle.getYear()).isEqualTo(UPDATED_YEAR);
        assertThat(testVehicle.getPlateNumber()).isEqualTo(UPDATED_PLATE_NUMBER);
        assertThat(testVehicle.isSupportHeavyTransport()).isEqualTo(UPDATED_SUPPORT_HEAVY_TRANSPORT);
        assertThat(testVehicle.getVinNumber()).isEqualTo(UPDATED_VIN_NUMBER);
        assertThat(testVehicle.getRegistrationExpDate()).isEqualTo(UPDATED_REGISTRATION_EXP_DATE);

        // Validate the Vehicle in Elasticsearch
        verify(mockVehicleSearchRepository, times(1)).save(testVehicle);
    }

    @Test
    public void updateNonExistingVehicle() throws Exception {
        int databaseSizeBeforeUpdate = vehicleRepository.findAll().size();

        // Create the Vehicle

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVehicleMockMvc.perform(put("/api/vehicles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vehicle)))
            .andExpect(status().isBadRequest());

        // Validate the Vehicle in the database
        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Vehicle in Elasticsearch
        verify(mockVehicleSearchRepository, times(0)).save(vehicle);
    }

    @Test
    public void deleteVehicle() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle);

        int databaseSizeBeforeDelete = vehicleRepository.findAll().size();

        // Delete the vehicle
        restVehicleMockMvc.perform(delete("/api/vehicles/{id}", vehicle.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Vehicle> vehicleList = vehicleRepository.findAll();
        assertThat(vehicleList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Vehicle in Elasticsearch
        verify(mockVehicleSearchRepository, times(1)).deleteById(vehicle.getId());
    }

    @Test
    public void searchVehicle() throws Exception {
        // Initialize the database
        vehicleRepository.save(vehicle);
        when(mockVehicleSearchRepository.search(queryStringQuery("id:" + vehicle.getId())))
            .thenReturn(Collections.singletonList(vehicle));
        // Search the vehicle
        restVehicleMockMvc.perform(get("/api/_search/vehicles?query=id:" + vehicle.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vehicle.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].year").value(hasItem(DEFAULT_YEAR)))
            .andExpect(jsonPath("$.[*].plateNumber").value(hasItem(DEFAULT_PLATE_NUMBER)))
            .andExpect(jsonPath("$.[*].supportHeavyTransport").value(hasItem(DEFAULT_SUPPORT_HEAVY_TRANSPORT.booleanValue())))
            .andExpect(jsonPath("$.[*].vinNumber").value(hasItem(DEFAULT_VIN_NUMBER)))
            .andExpect(jsonPath("$.[*].registrationExpDate").value(hasItem(DEFAULT_REGISTRATION_EXP_DATE.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Vehicle.class);
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setId("id1");
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(vehicle1.getId());
        assertThat(vehicle1).isEqualTo(vehicle2);
        vehicle2.setId("id2");
        assertThat(vehicle1).isNotEqualTo(vehicle2);
        vehicle1.setId(null);
        assertThat(vehicle1).isNotEqualTo(vehicle2);
    }
}
