package com.a6raywa1cher.rescheduletsuspring.dao.repository;

import com.a6raywa1cher.rescheduletsuspring.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	Optional<User> getByUsername(String username);
}
