package com.driverapp.web.rest;

import com.driverapp.DriverApp;
import com.driverapp.domain.Driver;
import com.driverapp.repository.DriverRepository;
import com.driverapp.repository.search.DriverSearchRepository;
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

import com.driverapp.domain.enumeration.Status;
import com.driverapp.domain.enumeration.PhoneType;
/**
 * Integration tests for the {@link DriverResource} REST controller.
 */
@SpringBootTest(classes = DriverApp.class)
public class DriverResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMER = "BBBBBBBBBB";

    private static final Status DEFAULT_STATUS = Status.INVITED;
    private static final Status UPDATED_STATUS = Status.CONFIRMED;

    private static final String DEFAULT_ONE_TIME_CODE = "AAAAAAAAAA";
    private static final String UPDATED_ONE_TIME_CODE = "BBBBBBBBBB";

    private static final Instant DEFAULT_ONE_TIME_EXPIRATION_TIME = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ONE_TIME_EXPIRATION_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final Instant SMALLER_ONE_TIME_EXPIRATION_TIME = Instant.ofEpochMilli(-1L);

    private static final String DEFAULT_DRIVER_LICENSE = "AAAAAAAAAA";
    private static final String UPDATED_DRIVER_LICENSE = "BBBBBBBBBB";

    private static final PhoneType DEFAULT_PHONE_TYPE = PhoneType.IPHONE;
    private static final PhoneType UPDATED_PHONE_TYPE = PhoneType.ANDROID;

    private static final Boolean DEFAULT_DUI_CONVICTION = false;
    private static final Boolean UPDATED_DUI_CONVICTION = true;

    private static final Boolean DEFAULT_FELONY_CONVICTION = false;
    private static final Boolean UPDATED_FELONY_CONVICTION = true;

    @Autowired
    private DriverRepository driverRepository;

    /**
     * This repository is mocked in the com.driverapp.repository.search test package.
     *
     * @see com.driverapp.repository.search.DriverSearchRepositoryMockConfiguration
     */
    @Autowired
    private DriverSearchRepository mockDriverSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restDriverMockMvc;

    private Driver driver;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final DriverResource driverResource = new DriverResource(driverRepository, mockDriverSearchRepository);
        this.restDriverMockMvc = MockMvcBuilders.standaloneSetup(driverResource)
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
    public static Driver createEntity() {
        Driver driver = new Driver()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .email(DEFAULT_EMAIL)
            .password(DEFAULT_PASSWORD)
            .phoneNumer(DEFAULT_PHONE_NUMER)
            .status(DEFAULT_STATUS)
            .oneTimeCode(DEFAULT_ONE_TIME_CODE)
            .oneTimeExpirationTime(DEFAULT_ONE_TIME_EXPIRATION_TIME)
            .driverLicense(DEFAULT_DRIVER_LICENSE)
            .phoneType(DEFAULT_PHONE_TYPE)
            .duiConviction(DEFAULT_DUI_CONVICTION)
            .felonyConviction(DEFAULT_FELONY_CONVICTION);
        return driver;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Driver createUpdatedEntity() {
        Driver driver = new Driver()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .password(UPDATED_PASSWORD)
            .phoneNumer(UPDATED_PHONE_NUMER)
            .status(UPDATED_STATUS)
            .oneTimeCode(UPDATED_ONE_TIME_CODE)
            .oneTimeExpirationTime(UPDATED_ONE_TIME_EXPIRATION_TIME)
            .driverLicense(UPDATED_DRIVER_LICENSE)
            .phoneType(UPDATED_PHONE_TYPE)
            .duiConviction(UPDATED_DUI_CONVICTION)
            .felonyConviction(UPDATED_FELONY_CONVICTION);
        return driver;
    }

    @BeforeEach
    public void initTest() {
        driverRepository.deleteAll();
        driver = createEntity();
    }

    @Test
    public void createDriver() throws Exception {
        int databaseSizeBeforeCreate = driverRepository.findAll().size();

        // Create the Driver
        restDriverMockMvc.perform(post("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isCreated());

        // Validate the Driver in the database
        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeCreate + 1);
        Driver testDriver = driverList.get(driverList.size() - 1);
        assertThat(testDriver.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testDriver.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(testDriver.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testDriver.getPassword()).isEqualTo(DEFAULT_PASSWORD);
        assertThat(testDriver.getPhoneNumer()).isEqualTo(DEFAULT_PHONE_NUMER);
        assertThat(testDriver.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testDriver.getOneTimeCode()).isEqualTo(DEFAULT_ONE_TIME_CODE);
        assertThat(testDriver.getOneTimeExpirationTime()).isEqualTo(DEFAULT_ONE_TIME_EXPIRATION_TIME);
        assertThat(testDriver.getDriverLicense()).isEqualTo(DEFAULT_DRIVER_LICENSE);
        assertThat(testDriver.getPhoneType()).isEqualTo(DEFAULT_PHONE_TYPE);
        assertThat(testDriver.isDuiConviction()).isEqualTo(DEFAULT_DUI_CONVICTION);
        assertThat(testDriver.isFelonyConviction()).isEqualTo(DEFAULT_FELONY_CONVICTION);

        // Validate the Driver in Elasticsearch
        verify(mockDriverSearchRepository, times(1)).save(testDriver);
    }

    @Test
    public void createDriverWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = driverRepository.findAll().size();

        // Create the Driver with an existing ID
        driver.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restDriverMockMvc.perform(post("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isBadRequest());

        // Validate the Driver in the database
        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeCreate);

        // Validate the Driver in Elasticsearch
        verify(mockDriverSearchRepository, times(0)).save(driver);
    }


    @Test
    public void checkFirstNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = driverRepository.findAll().size();
        // set the field null
        driver.setFirstName(null);

        // Create the Driver, which fails.

        restDriverMockMvc.perform(post("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isBadRequest());

        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkLastNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = driverRepository.findAll().size();
        // set the field null
        driver.setLastName(null);

        // Create the Driver, which fails.

        restDriverMockMvc.perform(post("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isBadRequest());

        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = driverRepository.findAll().size();
        // set the field null
        driver.setEmail(null);

        // Create the Driver, which fails.

        restDriverMockMvc.perform(post("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isBadRequest());

        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkDriverLicenseIsRequired() throws Exception {
        int databaseSizeBeforeTest = driverRepository.findAll().size();
        // set the field null
        driver.setDriverLicense(null);

        // Create the Driver, which fails.

        restDriverMockMvc.perform(post("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isBadRequest());

        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllDrivers() throws Exception {
        // Initialize the database
        driverRepository.save(driver);

        // Get all the driverList
        restDriverMockMvc.perform(get("/api/drivers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(driver.getId())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME.toString())))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME.toString())))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL.toString())))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD.toString())))
            .andExpect(jsonPath("$.[*].phoneNumer").value(hasItem(DEFAULT_PHONE_NUMER.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].oneTimeCode").value(hasItem(DEFAULT_ONE_TIME_CODE.toString())))
            .andExpect(jsonPath("$.[*].oneTimeExpirationTime").value(hasItem(DEFAULT_ONE_TIME_EXPIRATION_TIME.toString())))
            .andExpect(jsonPath("$.[*].driverLicense").value(hasItem(DEFAULT_DRIVER_LICENSE.toString())))
            .andExpect(jsonPath("$.[*].phoneType").value(hasItem(DEFAULT_PHONE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].duiConviction").value(hasItem(DEFAULT_DUI_CONVICTION.booleanValue())))
            .andExpect(jsonPath("$.[*].felonyConviction").value(hasItem(DEFAULT_FELONY_CONVICTION.booleanValue())));
    }
    
    @Test
    public void getDriver() throws Exception {
        // Initialize the database
        driverRepository.save(driver);

        // Get the driver
        restDriverMockMvc.perform(get("/api/drivers/{id}", driver.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(driver.getId()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME.toString()))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME.toString()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL.toString()))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD.toString()))
            .andExpect(jsonPath("$.phoneNumer").value(DEFAULT_PHONE_NUMER.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.oneTimeCode").value(DEFAULT_ONE_TIME_CODE.toString()))
            .andExpect(jsonPath("$.oneTimeExpirationTime").value(DEFAULT_ONE_TIME_EXPIRATION_TIME.toString()))
            .andExpect(jsonPath("$.driverLicense").value(DEFAULT_DRIVER_LICENSE.toString()))
            .andExpect(jsonPath("$.phoneType").value(DEFAULT_PHONE_TYPE.toString()))
            .andExpect(jsonPath("$.duiConviction").value(DEFAULT_DUI_CONVICTION.booleanValue()))
            .andExpect(jsonPath("$.felonyConviction").value(DEFAULT_FELONY_CONVICTION.booleanValue()));
    }

    @Test
    public void getNonExistingDriver() throws Exception {
        // Get the driver
        restDriverMockMvc.perform(get("/api/drivers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateDriver() throws Exception {
        // Initialize the database
        driverRepository.save(driver);

        int databaseSizeBeforeUpdate = driverRepository.findAll().size();

        // Update the driver
        Driver updatedDriver = driverRepository.findById(driver.getId()).get();
        updatedDriver
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .password(UPDATED_PASSWORD)
            .phoneNumer(UPDATED_PHONE_NUMER)
            .status(UPDATED_STATUS)
            .oneTimeCode(UPDATED_ONE_TIME_CODE)
            .oneTimeExpirationTime(UPDATED_ONE_TIME_EXPIRATION_TIME)
            .driverLicense(UPDATED_DRIVER_LICENSE)
            .phoneType(UPDATED_PHONE_TYPE)
            .duiConviction(UPDATED_DUI_CONVICTION)
            .felonyConviction(UPDATED_FELONY_CONVICTION);

        restDriverMockMvc.perform(put("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedDriver)))
            .andExpect(status().isOk());

        // Validate the Driver in the database
        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeUpdate);
        Driver testDriver = driverList.get(driverList.size() - 1);
        assertThat(testDriver.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testDriver.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testDriver.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testDriver.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testDriver.getPhoneNumer()).isEqualTo(UPDATED_PHONE_NUMER);
        assertThat(testDriver.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testDriver.getOneTimeCode()).isEqualTo(UPDATED_ONE_TIME_CODE);
        assertThat(testDriver.getOneTimeExpirationTime()).isEqualTo(UPDATED_ONE_TIME_EXPIRATION_TIME);
        assertThat(testDriver.getDriverLicense()).isEqualTo(UPDATED_DRIVER_LICENSE);
        assertThat(testDriver.getPhoneType()).isEqualTo(UPDATED_PHONE_TYPE);
        assertThat(testDriver.isDuiConviction()).isEqualTo(UPDATED_DUI_CONVICTION);
        assertThat(testDriver.isFelonyConviction()).isEqualTo(UPDATED_FELONY_CONVICTION);

        // Validate the Driver in Elasticsearch
        verify(mockDriverSearchRepository, times(1)).save(testDriver);
    }

    @Test
    public void updateNonExistingDriver() throws Exception {
        int databaseSizeBeforeUpdate = driverRepository.findAll().size();

        // Create the Driver

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDriverMockMvc.perform(put("/api/drivers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(driver)))
            .andExpect(status().isBadRequest());

        // Validate the Driver in the database
        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Driver in Elasticsearch
        verify(mockDriverSearchRepository, times(0)).save(driver);
    }

    @Test
    public void deleteDriver() throws Exception {
        // Initialize the database
        driverRepository.save(driver);

        int databaseSizeBeforeDelete = driverRepository.findAll().size();

        // Delete the driver
        restDriverMockMvc.perform(delete("/api/drivers/{id}", driver.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Driver> driverList = driverRepository.findAll();
        assertThat(driverList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Driver in Elasticsearch
        verify(mockDriverSearchRepository, times(1)).deleteById(driver.getId());
    }

    @Test
    public void searchDriver() throws Exception {
        // Initialize the database
        driverRepository.save(driver);
        when(mockDriverSearchRepository.search(queryStringQuery("id:" + driver.getId())))
            .thenReturn(Collections.singletonList(driver));
        // Search the driver
        restDriverMockMvc.perform(get("/api/_search/drivers?query=id:" + driver.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(driver.getId())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].phoneNumer").value(hasItem(DEFAULT_PHONE_NUMER)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].oneTimeCode").value(hasItem(DEFAULT_ONE_TIME_CODE)))
            .andExpect(jsonPath("$.[*].oneTimeExpirationTime").value(hasItem(DEFAULT_ONE_TIME_EXPIRATION_TIME.toString())))
            .andExpect(jsonPath("$.[*].driverLicense").value(hasItem(DEFAULT_DRIVER_LICENSE)))
            .andExpect(jsonPath("$.[*].phoneType").value(hasItem(DEFAULT_PHONE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].duiConviction").value(hasItem(DEFAULT_DUI_CONVICTION.booleanValue())))
            .andExpect(jsonPath("$.[*].felonyConviction").value(hasItem(DEFAULT_FELONY_CONVICTION.booleanValue())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Driver.class);
        Driver driver1 = new Driver();
        driver1.setId("id1");
        Driver driver2 = new Driver();
        driver2.setId(driver1.getId());
        assertThat(driver1).isEqualTo(driver2);
        driver2.setId("id2");
        assertThat(driver1).isNotEqualTo(driver2);
        driver1.setId(null);
        assertThat(driver1).isNotEqualTo(driver2);
    }
}
