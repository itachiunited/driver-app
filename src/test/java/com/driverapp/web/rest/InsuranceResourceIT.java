package com.driverapp.web.rest;

import com.driverapp.DriverApp;
import com.driverapp.domain.Insurance;
import com.driverapp.repository.InsuranceRepository;
import com.driverapp.repository.search.InsuranceSearchRepository;
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

import com.driverapp.domain.enumeration.InsuranceType;
/**
 * Integration tests for the {@link InsuranceResource} REST controller.
 */
@SpringBootTest(classes = DriverApp.class)
public class InsuranceResourceIT {

    private static final InsuranceType DEFAULT_TYPE = InsuranceType.AUTO;
    private static final InsuranceType UPDATED_TYPE = InsuranceType.HOME;

    private static final String DEFAULT_INSURANCE_PROVIDER = "AAAAAAAAAA";
    private static final String UPDATED_INSURANCE_PROVIDER = "BBBBBBBBBB";

    private static final Instant DEFAULT_INSURANCE_EXP_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_INSURANCE_EXP_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final Instant SMALLER_INSURANCE_EXP_DATE = Instant.ofEpochMilli(-1L);

    @Autowired
    private InsuranceRepository insuranceRepository;

    /**
     * This repository is mocked in the com.driverapp.repository.search test package.
     *
     * @see com.driverapp.repository.search.InsuranceSearchRepositoryMockConfiguration
     */
    @Autowired
    private InsuranceSearchRepository mockInsuranceSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restInsuranceMockMvc;

    private Insurance insurance;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final InsuranceResource insuranceResource = new InsuranceResource(insuranceRepository, mockInsuranceSearchRepository);
        this.restInsuranceMockMvc = MockMvcBuilders.standaloneSetup(insuranceResource)
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
    public static Insurance createEntity() {
        Insurance insurance = new Insurance()
            .type(DEFAULT_TYPE)
            .insuranceProvider(DEFAULT_INSURANCE_PROVIDER)
            .insuranceExpDate(DEFAULT_INSURANCE_EXP_DATE);
        return insurance;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Insurance createUpdatedEntity() {
        Insurance insurance = new Insurance()
            .type(UPDATED_TYPE)
            .insuranceProvider(UPDATED_INSURANCE_PROVIDER)
            .insuranceExpDate(UPDATED_INSURANCE_EXP_DATE);
        return insurance;
    }

    @BeforeEach
    public void initTest() {
        insuranceRepository.deleteAll();
        insurance = createEntity();
    }

    @Test
    public void createInsurance() throws Exception {
        int databaseSizeBeforeCreate = insuranceRepository.findAll().size();

        // Create the Insurance
        restInsuranceMockMvc.perform(post("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(insurance)))
            .andExpect(status().isCreated());

        // Validate the Insurance in the database
        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeCreate + 1);
        Insurance testInsurance = insuranceList.get(insuranceList.size() - 1);
        assertThat(testInsurance.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testInsurance.getInsuranceProvider()).isEqualTo(DEFAULT_INSURANCE_PROVIDER);
        assertThat(testInsurance.getInsuranceExpDate()).isEqualTo(DEFAULT_INSURANCE_EXP_DATE);

        // Validate the Insurance in Elasticsearch
        verify(mockInsuranceSearchRepository, times(1)).save(testInsurance);
    }

    @Test
    public void createInsuranceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = insuranceRepository.findAll().size();

        // Create the Insurance with an existing ID
        insurance.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restInsuranceMockMvc.perform(post("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(insurance)))
            .andExpect(status().isBadRequest());

        // Validate the Insurance in the database
        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeCreate);

        // Validate the Insurance in Elasticsearch
        verify(mockInsuranceSearchRepository, times(0)).save(insurance);
    }


    @Test
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = insuranceRepository.findAll().size();
        // set the field null
        insurance.setType(null);

        // Create the Insurance, which fails.

        restInsuranceMockMvc.perform(post("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(insurance)))
            .andExpect(status().isBadRequest());

        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkInsuranceProviderIsRequired() throws Exception {
        int databaseSizeBeforeTest = insuranceRepository.findAll().size();
        // set the field null
        insurance.setInsuranceProvider(null);

        // Create the Insurance, which fails.

        restInsuranceMockMvc.perform(post("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(insurance)))
            .andExpect(status().isBadRequest());

        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkInsuranceExpDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = insuranceRepository.findAll().size();
        // set the field null
        insurance.setInsuranceExpDate(null);

        // Create the Insurance, which fails.

        restInsuranceMockMvc.perform(post("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(insurance)))
            .andExpect(status().isBadRequest());

        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllInsurances() throws Exception {
        // Initialize the database
        insuranceRepository.save(insurance);

        // Get all the insuranceList
        restInsuranceMockMvc.perform(get("/api/insurances?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(insurance.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].insuranceProvider").value(hasItem(DEFAULT_INSURANCE_PROVIDER.toString())))
            .andExpect(jsonPath("$.[*].insuranceExpDate").value(hasItem(DEFAULT_INSURANCE_EXP_DATE.toString())));
    }
    
    @Test
    public void getInsurance() throws Exception {
        // Initialize the database
        insuranceRepository.save(insurance);

        // Get the insurance
        restInsuranceMockMvc.perform(get("/api/insurances/{id}", insurance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(insurance.getId()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.insuranceProvider").value(DEFAULT_INSURANCE_PROVIDER.toString()))
            .andExpect(jsonPath("$.insuranceExpDate").value(DEFAULT_INSURANCE_EXP_DATE.toString()));
    }

    @Test
    public void getNonExistingInsurance() throws Exception {
        // Get the insurance
        restInsuranceMockMvc.perform(get("/api/insurances/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateInsurance() throws Exception {
        // Initialize the database
        insuranceRepository.save(insurance);

        int databaseSizeBeforeUpdate = insuranceRepository.findAll().size();

        // Update the insurance
        Insurance updatedInsurance = insuranceRepository.findById(insurance.getId()).get();
        updatedInsurance
            .type(UPDATED_TYPE)
            .insuranceProvider(UPDATED_INSURANCE_PROVIDER)
            .insuranceExpDate(UPDATED_INSURANCE_EXP_DATE);

        restInsuranceMockMvc.perform(put("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedInsurance)))
            .andExpect(status().isOk());

        // Validate the Insurance in the database
        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeUpdate);
        Insurance testInsurance = insuranceList.get(insuranceList.size() - 1);
        assertThat(testInsurance.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testInsurance.getInsuranceProvider()).isEqualTo(UPDATED_INSURANCE_PROVIDER);
        assertThat(testInsurance.getInsuranceExpDate()).isEqualTo(UPDATED_INSURANCE_EXP_DATE);

        // Validate the Insurance in Elasticsearch
        verify(mockInsuranceSearchRepository, times(1)).save(testInsurance);
    }

    @Test
    public void updateNonExistingInsurance() throws Exception {
        int databaseSizeBeforeUpdate = insuranceRepository.findAll().size();

        // Create the Insurance

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInsuranceMockMvc.perform(put("/api/insurances")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(insurance)))
            .andExpect(status().isBadRequest());

        // Validate the Insurance in the database
        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Insurance in Elasticsearch
        verify(mockInsuranceSearchRepository, times(0)).save(insurance);
    }

    @Test
    public void deleteInsurance() throws Exception {
        // Initialize the database
        insuranceRepository.save(insurance);

        int databaseSizeBeforeDelete = insuranceRepository.findAll().size();

        // Delete the insurance
        restInsuranceMockMvc.perform(delete("/api/insurances/{id}", insurance.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Insurance> insuranceList = insuranceRepository.findAll();
        assertThat(insuranceList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Insurance in Elasticsearch
        verify(mockInsuranceSearchRepository, times(1)).deleteById(insurance.getId());
    }

    @Test
    public void searchInsurance() throws Exception {
        // Initialize the database
        insuranceRepository.save(insurance);
        when(mockInsuranceSearchRepository.search(queryStringQuery("id:" + insurance.getId())))
            .thenReturn(Collections.singletonList(insurance));
        // Search the insurance
        restInsuranceMockMvc.perform(get("/api/_search/insurances?query=id:" + insurance.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(insurance.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].insuranceProvider").value(hasItem(DEFAULT_INSURANCE_PROVIDER)))
            .andExpect(jsonPath("$.[*].insuranceExpDate").value(hasItem(DEFAULT_INSURANCE_EXP_DATE.toString())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Insurance.class);
        Insurance insurance1 = new Insurance();
        insurance1.setId("id1");
        Insurance insurance2 = new Insurance();
        insurance2.setId(insurance1.getId());
        assertThat(insurance1).isEqualTo(insurance2);
        insurance2.setId("id2");
        assertThat(insurance1).isNotEqualTo(insurance2);
        insurance1.setId(null);
        assertThat(insurance1).isNotEqualTo(insurance2);
    }
}
