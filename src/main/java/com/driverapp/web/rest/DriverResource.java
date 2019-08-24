package com.driverapp.web.rest;

import com.driverapp.domain.DeviceDetails;
import com.driverapp.domain.Driver;
import com.driverapp.domain.enumeration.Status;
import com.driverapp.repository.DeviceDetailsRepository;
import com.driverapp.repository.DriverRepository;
import com.driverapp.repository.search.DriverSearchRepository;
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

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing {@link com.driverapp.domain.Driver}.
 */
@RestController
@RequestMapping("/api")
public class DriverResource {

    private final Logger log = LoggerFactory.getLogger(DriverResource.class);

    private static final String ENTITY_NAME = "driverAppDriver";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DriverRepository driverRepository;

    private final DriverSearchRepository driverSearchRepository;

    private final DeviceDetailsRepository deviceDetailsRepository;

    public DriverResource(DriverRepository driverRepository, DriverSearchRepository driverSearchRepository, DeviceDetailsRepository deviceDetailsRepository) {
        this.driverRepository = driverRepository;
        this.driverSearchRepository = driverSearchRepository;
        this.deviceDetailsRepository = deviceDetailsRepository;
    }

    /**
     * {@code POST  /drivers} : Create a new driver.
     *
     * @param driver the driver to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new driver, or with status {@code 400 (Bad Request)} if the driver has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/drivers")
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody Driver driver) throws URISyntaxException {
        log.debug("REST request to save Driver : {}", driver);
        if (driver.getId() != null) {
            throw new BadRequestAlertException("A new driver cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Driver result = driverRepository.save(driver);
        driverSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/drivers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code POST  /drivers-phonenumber-capture} : Create a new driver with PENDING and OTC Code Generated.
     *
     * @param driver the Driver to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new nGUser, or with status {@code 400 (Bad Request)} if the nGUser has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/drivers-phonenumber-capture")
    public ResponseEntity<Driver> createNGUserWithPhoneNumber(@Valid @RequestBody Driver driver) throws URISyntaxException {
        log.debug("REST request to save Driver : {}", driver);
        if (driver.getId() != null) {
            throw new BadRequestAlertException("A new driver cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Random rnd = new Random();
        int oneTimeCode = 100000 + rnd.nextInt(900000);
        log.debug("OTC --> "+oneTimeCode);
        Calendar otcExpiration = Calendar.getInstance();
        otcExpiration.add(Calendar.MINUTE, 30);

        driver.setOneTimeCode(String.valueOf(oneTimeCode));
        driver.setOneTimeExpirationTime(otcExpiration.toInstant());

        driver.setStatus(Status.INVITED);

        Driver result = driverRepository.save(driver);
        driverSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/drivers-phonenumber-capture/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PostMapping("/drivers-verify-token")
    public ResponseEntity<Driver> createNGUserVerifyToken(@Valid @RequestBody Map driverMap) throws URISyntaxException {
        log.debug("REST request to save Driver : {}", driverMap);

        // Change this to session
        if (driverMap.get("id") == null) {
            throw new BadRequestAlertException("Request Needs to Have an ID", ENTITY_NAME, "idnotpresent");
        }

        Driver driverFromRep = driverRepository.findUserById((String)driverMap.get("id"));
        // Check User Status
        if(!Status.INVITED.equals(driverFromRep.getStatus()))
        {
            throw new BadRequestAlertException("User Already Confirmed",ENTITY_NAME,"alreadyConfirmed");
            // Redirect to Dashboard
        }
        // Compare OTC Code & TimeStamp

        Instant currentTime = Instant.now();
        if(currentTime.isAfter(driverFromRep.getOneTimeExpirationTime()))
        {
            throw new BadRequestAlertException("Code Expired. Please request for another code",ENTITY_NAME,"codeExpired");
        }

        if(null!=driverMap.get("oneTimeCode") && ((String)driverMap.get("oneTimeCode")).equalsIgnoreCase(driverFromRep.getOneTimeCode()))
        {
            System.out.println("Valid Code. Customer Authenticated");

            DeviceDetails deviceDetails = new DeviceDetails();
            deviceDetails.setDeviceId("testWindows");
            DeviceDetails deviceDetailsFromRep = deviceDetailsRepository.save(deviceDetails);

            Set<DeviceDetails> devices = new HashSet<>();
            devices.add(deviceDetailsFromRep);

            driverFromRep.setDevices(devices);

            driverFromRep.setStatus(Status.CONFIRMED);
        }
        else
        {
            throw new BadRequestAlertException("Code Mismatch. Please reenter the code",ENTITY_NAME,"codeMisMatch");
        }
        Driver result = driverRepository.save(driverFromRep);
        driverSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/drivers-verify-token/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }


    /**
     * {@code PUT  /drivers} : Updates an existing driver.
     *
     * @param driver the driver to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated driver,
     * or with status {@code 400 (Bad Request)} if the driver is not valid,
     * or with status {@code 500 (Internal Server Error)} if the driver couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/drivers")
    public ResponseEntity<Driver> updateDriver(@Valid @RequestBody Driver driver) throws URISyntaxException {
        log.debug("REST request to update Driver : {}", driver);
        if (driver.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Driver result = driverRepository.save(driver);
        driverSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, driver.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /drivers} : get all the drivers.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of drivers in body.
     */
    @GetMapping("/drivers")
    public List<Driver> getAllDrivers() {
        log.debug("REST request to get all Drivers");
        return driverRepository.findAll();
    }

    /**
     * {@code GET  /drivers/:id} : get the "id" driver.
     *
     * @param id the id of the driver to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the driver, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/drivers/{id}")
    public ResponseEntity<Driver> getDriver(@PathVariable String id) {
        log.debug("REST request to get Driver : {}", id);
        Optional<Driver> driver = driverRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(driver);
    }

    /**
     * {@code DELETE  /drivers/:id} : delete the "id" driver.
     *
     * @param id the id of the driver to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) {
        log.debug("REST request to delete Driver : {}", id);
        driverRepository.deleteById(id);
        driverSearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /_search/drivers?query=:query} : search for the driver corresponding
     * to the query.
     *
     * @param query the query of the driver search.
     * @return the result of the search.
     */
    @GetMapping("/_search/drivers")
    public List<Driver> searchDrivers(@RequestParam String query) {
        log.debug("REST request to search Drivers for query {}", query);
        return StreamSupport
            .stream(driverSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

}
