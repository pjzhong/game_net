package org.pj.msg;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.pj.msg.PersonProto.Person;
import org.pj.msg.PersonProto.Person.PhoneNumber;
import org.pj.msg.PersonProto.Person.PhoneType;

public class ProtoTest {

  @Test
  public void buildPerson() {
    Person john = Person.newBuilder()
        .setId(1234)
        .setName("John Doe")
        .setEmail("jdoe@example.com")
        .addPhones(
            PhoneNumber.newBuilder()
                .setNumber("555-4321").setType(PhoneType.HOME))
        .build();
    System.out.println(john);
  }

  @Test
  public void parsePerson() throws InvalidProtocolBufferException {
    Person john = Person.newBuilder()
        .setId(1234)
        .setName("John Doe")
        .setEmail("jdoe@example.com")
        .addPhones(
            PhoneNumber.newBuilder()
                .setNumber("555-4321").setType(PhoneType.HOME))
        .build();
    byte[] bytes = john.toByteArray();
    System.out.println(Arrays.toString(bytes));

    Person parsedJohn = Person.parseFrom(bytes);
    System.out.println(parsedJohn);
    Assert.assertEquals(john, parsedJohn);
  }

}
