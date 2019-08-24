package com.driverapp.web.rest;

import com.driverapp.DriverApp;
import com.driverapp.domain.Business;
import com.driverapp.repository.BusinessRepository;
import com.driverapp.repository.search.BusinessSearchRepository;
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
 * Integration tests for the {@link BusinessResource} REST controller.
 */
@SpringBootTest(classes = DriverApp.class)
public class BusinessResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_PHONE_NUMBER = "BBBBBBBBBB";

    @Autowired
    private BusinessRepository businessRepository;

    /**
     * This repository is mocked in the com.driverapp.repository.search test package.
     *
     * @see com.driverapp.repository.search.BusinessSearchRepositoryMockConfiguration
     */
    @Autowired
    private BusinessSearchRepository mockBusinessSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restBusinessMockMvc;

    private Business business;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BusinessResource businessResource = new BusinessResource(businessRepository, mockBusinessSearchRepository);
        this.restBusinessMockMvc = MockMvcBuilders.standaloneSetup(businessResource)
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
    public static Business createEntity() {
        Business business = new Business()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .address(DEFAULT_ADDRESS)
            .phoneNumber(DEFAULT_PHONE_NUMBER);
        return business;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Business createUpdatedEntity() {
        Business business = new Business()
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .address(UPDATED_ADDRESS)
            .phoneNumber(UPDATED_PHONE_NUMBER);
        return business;
    }

    @BeforeEach
    public void initTest() {
        businessRepository.deleteAll();
        business = createEntity();
    }

    @Test
    public void createBusiness() throws Exception {
        int databaseSizeBeforeCreate = businessRepository.findAll().size();

        // Create the Business
        restBusinessMockMvc.perform(post("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(business)))
            .andExpect(status().isCreated());

        // Validate the Business in the database
        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeCreate + 1);
        Business testBusiness = businessList.get(businessList.size() - 1);
        assertThat(testBusiness.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBusiness.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testBusiness.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testBusiness.getPhoneNumber()).isEqualTo(DEFAULT_PHONE_NUMBER);

        // Validate the Business in Elasticsearch
        verify(mockBusinessSearchRepository, times(1)).save(testBusiness);
    }

    @Test
    public void createBusinessWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = businessRepository.findAll().size();

        // Create the Business with an existing ID
        business.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restBusinessMockMvc.perform(post("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(business)))
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeCreate);

        // Validate the Business in Elasticsearch
        verify(mockBusinessSearchRepository, times(0)).save(business);
    }


    @Test
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = businessRepository.findAll().size();
        // set the field null
        business.setName(null);

        // Create the Business, which fails.

        restBusinessMockMvc.perform(post("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(business)))
            .andExpect(status().isBadRequest());

        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = businessRepository.findAll().size();
        // set the field null
        business.setType(null);

        // Create the Business, which fails.

        restBusinessMockMvc.perform(post("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(business)))
            .andExpect(status().isBadRequest());

        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkAddressIsRequired() throws Exception {
        int databaseSizeBeforeTest = businessRepository.findAll().size();
        // set the field null
        business.setAddress(null);

        // Create the Business, which fails.

        restBusinessMockMvc.perform(post("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(business)))
            .andExpect(status().isBadRequest());

        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllBusinesses() throws Exception {
        // Initialize the database
        businessRepository.save(business);

        // Get all the businessList
        restBusinessMockMvc.perform(get("/api/businesses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(business.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS.toString())))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER.toString())));
    }
    
    @Test
    public void getBusiness() throws Exception {
        // Initialize the database
        businessRepository.save(business);

        // Get the business
        restBusinessMockMvc.perform(get("/api/businesses/{id}", business.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(business.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS.toString()))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_PHONE_NUMBER.toString()));
    }

    @Test
    public void getNonExistingBusiness() throws Exception {
        // Get the business
        restBusinessMockMvc.perform(get("/api/businesses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateBusiness() throws Exception {
        // Initialize the database
        businessRepository.save(business);

        int databaseSizeBeforeUpdate = businessRepository.findAll().size();

        // Update the business
        Business updatedBusiness = businessRepository.findById(business.getId()).get();
        updatedBusiness
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .address(UPDATED_ADDRESS)
            .phoneNumber(UPDATED_PHONE_NUMBER);

        restBusinessMockMvc.perform(put("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedBusiness)))
            .andExpect(status().isOk());

        // Validate the Business in the database
        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeUpdate);
        Business testBusiness = businessList.get(businessList.size() - 1);
        assertThat(testBusiness.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBusiness.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBusiness.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testBusiness.getPhoneNumber()).isEqualTo(UPDATED_PHONE_NUMBER);

        // Validate the Business in Elasticsearch
        verify(mockBusinessSearchRepository, times(1)).save(testBusiness);
    }

    @Test
    public void updateNonExistingBusiness() throws Exception {
        int databaseSizeBeforeUpdate = businessRepository.findAll().size();

        // Create the Business

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBusinessMockMvc.perform(put("/api/businesses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(business)))
            .andExpect(status().isBadRequest());

        // Validate the Business in the database
        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Business in Elasticsearch
        verify(mockBusinessSearchRepository, times(0)).save(business);
    }

    @Test
    public void deleteBusiness() throws Exception {
        // Initialize the database
        businessRepository.save(business);

        int databaseSizeBeforeDelete = businessRepository.findAll().size();

        // Delete the business
        restBusinessMockMvc.perform(delete("/api/businesses/{id}", business.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Business> businessList = businessRepository.findAll();
        assertThat(businessList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Business in Elasticsearch
        verify(mockBusinessSearchRepository, times(1)).deleteById(business.getId());
    }

    @Test
    public void searchBusiness() throws Exception {
        // Initialize the database
        businessRepository.save(business);
        when(mockBusinessSearchRepository.search(queryStringQuery("id:" + business.getId())))
            .thenReturn(Collections.singletonList(business));
        // Search the business
        restBusinessMockMvc.perform(get("/api/_search/businesses?query=id:" + business.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(business.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_PHONE_NUMBER)));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Business.class);
        Business business1 = new Business();
        business1.setId("id1");
        Business business2 = new Business();
        business2.setId(business1.getId());
        assertThat(business1).isEqualTo(business2);
        business2.setId("id2");
        assertThat(business1).isNotEqualTo(business2);
        business1.setId(null);
        assertThat(business1).isNotEqualTo(business2);
    }
}
