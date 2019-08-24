package com.driverapp.web.rest;

import com.driverapp.domain.Business;
import com.driverapp.repository.BusinessRepository;
import com.driverapp.repository.search.BusinessSearchRepository;
import com.driverapp.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing {@link com.driverapp.domain.Business}.
 */
@RestController
@RequestMapping("/api")
public class BusinessResource {

    private final Logger log = LoggerFactory.getLogger(BusinessResource.class);

    private static final String ENTITY_NAME = "driverAppBusiness";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BusinessRepository businessRepository;

    private final BusinessSearchRepository businessSearchRepository;

    public BusinessResource(BusinessRepository businessRepository, BusinessSearchRepository businessSearchRepository) {
        this.businessRepository = businessRepository;
        this.businessSearchRepository = businessSearchRepository;
    }

    /**
     * {@code POST  /businesses} : Create a new business.
     *
     * @param business the business to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new business, or with status {@code 400 (Bad Request)} if the business has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/businesses")
    public ResponseEntity<Business> createBusiness(@Valid @RequestBody Business business) throws URISyntaxException {
        log.debug("REST request to save Business : {}", business);
        if (business.getId() != null) {
            throw new BadRequestAlertException("A new business cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Business result = businessRepository.save(business);
        businessSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/businesses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /businesses} : Updates an existing business.
     *
     * @param business the business to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated business,
     * or with status {@code 400 (Bad Request)} if the business is not valid,
     * or with status {@code 500 (Internal Server Error)} if the business couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/businesses")
    public ResponseEntity<Business> updateBusiness(@Valid @RequestBody Business business) throws URISyntaxException {
        log.debug("REST request to update Business : {}", business);
        if (business.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Business result = businessRepository.save(business);
        businessSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, business.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /businesses} : get all the businesses.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of businesses in body.
     */
    @GetMapping("/businesses")
    public List<Business> getAllBusinesses() {
        log.debug("REST request to get all Businesses");
        return businessRepository.findAll();
    }

    /**
     * {@code GET  /businesses/:id} : get the "id" business.
     *
     * @param id the id of the business to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the business, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/businesses/{id}")
    public ResponseEntity<Business> getBusiness(@PathVariable String id) {
        log.debug("REST request to get Business : {}", id);
        Optional<Business> business = businessRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(business);
    }

    /**
     * {@code DELETE  /businesses/:id} : delete the "id" business.
     *
     * @param id the id of the business to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/businesses/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable String id) {
        log.debug("REST request to delete Business : {}", id);
        businessRepository.deleteById(id);
        businessSearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /_search/businesses?query=:query} : search for the business corresponding
     * to the query.
     *
     * @param query the query of the business search.
     * @return the result of the search.
     */
    @GetMapping("/_search/businesses")
    public List<Business> searchBusinesses(@RequestParam String query) {
        log.debug("REST request to search Businesses for query {}", query);
        return StreamSupport
            .stream(businessSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
