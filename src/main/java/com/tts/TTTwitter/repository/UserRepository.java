package com.tts.TTTwitter.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tts.TTTwitter.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
  public User findByUsername(String username);

}
