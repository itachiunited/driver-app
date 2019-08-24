package com.driverapp.web.rest;

import com.driverapp.domain.Services;
import com.driverapp.repository.ServicesRepository;
import com.driverapp.repository.search.ServicesSearchRepository;
import com.driverapp.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing {@link com.driverapp.domain.Services}.
 */
@RestController
@RequestMapping("/api")
public class ServicesResource {

    private final Logger log = LoggerFactory.getLogger(ServicesResource.class);

    private static final String ENTITY_NAME = "driverAppServices";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ServicesRepository servicesRepository;

    private final ServicesSearchRepository servicesSearchRepository;

    public ServicesResource(ServicesRepository servicesRepository, ServicesSearchRepository servicesSearchRepository) {
        this.servicesRepository = servicesRepository;
        this.servicesSearchRepository = servicesSearchRepository;
    }

    /**
     * {@code POST  /services} : Create a new services.
     *
     * @param services the services to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new services, or with status {@code 400 (Bad Request)} if the services has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/services")
    public ResponseEntity<Services> createServices(@RequestBody Services services) throws URISyntaxException {
        log.debug("REST request to save Services : {}", services);
        if (services.getId() != null) {
            throw new BadRequestAlertException("A new services cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Services result = servicesRepository.save(services);
        servicesSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /services} : Updates an existing services.
     *
     * @param services the services to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated services,
     * or with status {@code 400 (Bad Request)} if the services is not valid,
     * or with status {@code 500 (Internal Server Error)} if the services couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/services")
    public ResponseEntity<Services> updateServices(@RequestBody Services services) throws URISyntaxException {
        log.debug("REST request to update Services : {}", services);
        if (services.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Services result = servicesRepository.save(services);
        servicesSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, services.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /services} : get all the services.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of services in body.
     */
    @GetMapping("/services")
    public List<Services> getAllServices() {
        log.debug("REST request to get all Services");
        return servicesRepository.findAll();
    }

    /**
     * {@code GET  /services/:id} : get the "id" services.
     *
     * @param id the id of the services to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the services, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/services/{id}")
    public ResponseEntity<Services> getServices(@PathVariable String id) {
        log.debug("REST request to get Services : {}", id);
        Optional<Services> services = servicesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(services);
    }

    /**
     * {@code DELETE  /services/:id} : delete the "id" services.
     *
     * @param id the id of the services to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteServices(@PathVariable String id) {
        log.debug("REST request to delete Services : {}", id);
        servicesRepository.deleteById(id);
        servicesSearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /_search/services?query=:query} : search for the services corresponding
     * to the query.
     *
     * @param query the query of the services search.
     * @return the result of the search.
     */
    @GetMapping("/_search/services")
    public List<Services> searchServices(@RequestParam String query) {
        log.debug("REST request to search Services for query {}", query);
        return StreamSupport
            .stream(servicesSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
