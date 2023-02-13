package cca.database;

public class Person {

    int ssn;
    String name;
    String gender;
    String dateOfBirth;
    String dateOfDeath;

    public Person(int ssn, String name, String gender, String dateOfBirth, String dateOfDeath) {
        this.ssn = ssn;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
    }

    public Person() {
        this.ssn = 0;
        this.name = "";
        this.gender = "x";
        this.dateOfBirth = "1900-01-01";
        this.dateOfDeath = null;
    }
    
    public int getSsn() {
        return ssn;
    }

    public String getName() {
        return name;
    }    
    
    public String getGender() {
        return gender;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }    

    public String getDateOfDeath() {
        return dateOfDeath;
    }

    public void setSsn(int ssn) {
        this.ssn = ssn;
    }

    public void setName(String name) {
        this.name = name;
    }    
    
    public void getGender(String gender) {
        this.gender = gender;
    }
    
    public void getDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }    

    public void setDateOfDeath(String dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }
}
