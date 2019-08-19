package net.joeclark.webapps.granite.agency;

public class Agency {
    private String agencyName;
    private String city;
    private String state;
    private String phoneNumber;

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Agency() {
    }

    public Agency(String agencyName, String city, String state, String phoneNumber) {
        this.agencyName = agencyName;
        this.city = city;
        this.state = state;
        this.phoneNumber = phoneNumber;
    }
}
