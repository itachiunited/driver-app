package com.driverapp.repository;

import com.driverapp.domain.Services;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data MongoDB repository for the Services entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ServicesRepository extends MongoRepository<Services, String> {

}
