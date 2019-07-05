package Model;

import java.io.Serializable;

public class Driver implements Serializable
{
    private String id;
    private String name;
    private String mgr;
    private String cnic;
    private String mobile;
    private String address;
    private String email;
    private String online;

    public Driver(String dId, String DName, String DMgr, String cnic, String mobile, String address, String email, String online) {
        this.id = dId;
        this.name = DName;
        this.mgr = DMgr;
        this.cnic = cnic;
        this.mobile = mobile;
        this.address = address;
        this.email = email;
        this.online = online;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMgr() {
        return mgr;
    }

    public void setMgr(String mgr) {
        this.mgr = mgr;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
