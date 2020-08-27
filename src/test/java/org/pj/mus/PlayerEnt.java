package org.pj.mus;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.Map;

@Document(collation = "playerEnt")
public class PlayerEnt {

    @Id
    private long id;
    private String name;
    private Map<Integer, OneBox> oneBoxes = Collections.singletonMap(1, new OneBox());

    public PlayerEnt(long id) {
        this.id = id;
        this.name = String.valueOf(id);
    }

    public long getId() {
        return id;
    }

    public Map<Integer, OneBox> getOneBoxes() {
        return oneBoxes;
    }

    public String getName() {
        return name;
    }
}
