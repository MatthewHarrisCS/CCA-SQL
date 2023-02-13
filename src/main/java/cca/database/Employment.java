package cca.database;

public class Employment {
    int ssn;
    String employerName;
    int employerFEIN;
    String dateOfHire;
    String dateOfRetire;
    double serviceCredit;

    public Employment(int ssn, String employerName, int employerFEIN, String dateOfHire, String dateOfRetire, double serviceCredit) {
        this.ssn = ssn;
        this.employerName = employerName;
        this.employerFEIN = employerFEIN;
        this.dateOfHire = dateOfHire;
        this.dateOfRetire = dateOfRetire;
        this.serviceCredit = serviceCredit;
    }

    public Employment() {
        this.ssn = 0;
        this.employerName = "N/A";
        this.employerFEIN = 0;
        this.dateOfHire = "1900-01-01";
        this.dateOfRetire = null;
        this.serviceCredit = 0.0;
    }
    
    public int getSsn() {
        return ssn;
    }

    public String getEmployerName() {
        return employerName;
    }    
    
    public int getEmployerFEIN() {
        return employerFEIN;
    }
    
    public String getDateOfHire() {
        return dateOfHire;
    }    

    public String getDateOfRetire() {
        return dateOfRetire;
    }

    public double getServiceCredit() {
        return serviceCredit;
    }

    public void setSsn(int ssn) {
        this.ssn = ssn;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }    
    
    public void setEmployerFEIN(int employerFEIN) {
        this.employerFEIN = employerFEIN;
    }
    
    public void setDateOfHire(String dateOfHire) {
        this.dateOfHire = dateOfHire;
    }    

    public void setDateOfRetire(String dateOfRetire) {
        this.dateOfRetire = dateOfRetire;
    }

    public void setServiceCredit(double serviceCredit) {
        this.serviceCredit = serviceCredit;
    }
}
