package puf.example.td1;

/**
 * Created by Admin on 19/6/2016.
 */
public class City {
    String name;
    String id;
    public City(){
        name = id = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
