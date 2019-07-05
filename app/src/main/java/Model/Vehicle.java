package Model;

import java.io.Serializable;

public class Vehicle implements Serializable
{
    private String Id;
    private String owner;
    private String Model;
    private String name;
    private String vin;

    public Vehicle(String id, String owner, String model, String name, String vin) {
        Id = id;
        this.owner = owner;
        Model = model;
        this.name = name;
        this.vin = vin;
    }

    public String getVIN() {
        return vin;
    }

    public void setVIN(String VIN) {
        this.vin = VIN;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
