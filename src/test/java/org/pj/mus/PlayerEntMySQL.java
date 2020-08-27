package org.pj.mus;

import java.util.Collections;
import java.util.Map;

public class PlayerEntMySQL {

    private long id;
    private Map<Integer, OneBox> oneBoxes = Collections.singletonMap(1, new OneBox());
    private String oneBoxesJson;

    public long getId() {
        return id;
    }

    public Map<Integer, OneBox> getOneBoxes() {
        return oneBoxes;
    }

    public String getOneBoxesJson() {
        return oneBoxesJson;
    }
}
