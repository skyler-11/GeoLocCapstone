package com.example.geoloccapstone.object;

public class Model {
    String ssuID, firstName, lastName;

    public Model(){
    }

    public Model(String ssuID, String firstName, String lastName) {
        this.ssuID = ssuID;
        this.firstName = firstName;
        this.lastName = lastName;

    }

    public String getSsuID() {
        return ssuID;
    }

    public void setSsuID(String ssuID) {
        this.ssuID = ssuID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
