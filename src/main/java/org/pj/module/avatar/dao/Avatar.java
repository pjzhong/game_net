package org.pj.module.avatar.dao;

import org.pj.module.DBData;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "avatar")
public class Avatar implements DBData {

  private long id = 1;

  private String name;

  public Avatar() {
  }

  public Avatar(long id) {
    this.id = id;
    this.name = "name is " + id;
  }

  public long getId() {
    return id;
  }

  public Avatar setId(long id) {
    this.id = id;
    return this;
  }


  public String getName() {
    return name;
  }

  public Avatar setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public void check() {

  }
}
