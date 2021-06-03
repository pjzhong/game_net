package org.pj.module.avatar.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AvatarRepository extends MongoRepository<Avatar, Long> {

}
