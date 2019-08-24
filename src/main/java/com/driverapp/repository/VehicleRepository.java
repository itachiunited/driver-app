package com.driverapp.repository;

import com.driverapp.domain.Vehicle;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data MongoDB repository for the Vehicle entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

}
