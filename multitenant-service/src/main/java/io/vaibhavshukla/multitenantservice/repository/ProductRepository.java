package io.vaibhavshukla.multitenantservice.repository;

import io.vaibhavshukla.multitenantservice.entity.Product;
import org.springframework.data.repository.CrudRepository;


public interface ProductRepository extends CrudRepository<Product, Long> {

}