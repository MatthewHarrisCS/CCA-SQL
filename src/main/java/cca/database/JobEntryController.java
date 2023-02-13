package cca.database;

import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class JobEntryController implements Initializable {

    @FXML private Label nameField;
    @FXML private ChoiceBox<String> employerName;
    @FXML private Spinner<Integer> fein;
    @FXML private Spinner<Integer> hMonth;
    @FXML private Spinner<Integer> hDay;
    @FXML private Spinner<Integer> hYear;
    @FXML private Spinner<Integer> rMonth;
    @FXML private Spinner<Integer> rDay;
    @FXML private Spinner<Integer> rYear;
    @FXML private Spinner<Double> credits;
    @FXML private CheckBox retired;
    private int ssn;
    private String keyEmployer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        FilteredList<String> employerList = new FilteredList<>(App.employerList, s -> true);
        employerList.setPredicate(i -> i.charAt(0) != '(');
        employerName.setItems(employerList);

        // Set the spinner ranges and default values
        fein.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10000000, 999999999, 11111111));
        hMonth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12));
        hDay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31));
        hYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2999, 1999));
        rMonth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12));
        rDay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31));
        rYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2999, 1999));
        credits.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1000.0));
        
    }

    public void setName(int ssn) {

        this.ssn = ssn;

        try {
            ResultSet q = App.stmt.executeQuery("SELECT FirstName, LastName FROM PersonFX WHERE SSN = " + ssn + ";");

            while(q.next()) {

                nameField.setText(q.getString("FirstName") + " " + q.getString("LastName"));
            }

        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }
    }

    public void setJob(Boolean newJob) {

        String enStr = employerName.getValue();
        if (enStr == null) {
            enStr = "NULL";
        } else {
            enStr = "'" + enStr + "'";
        }

        String dorStr;
        if (retired.isSelected()) {
            dorStr = "NULL";
        } else {
            dorStr = "'".concat(rYear.getValue().toString()).concat("-")
                .concat(rMonth.getValue().toString()).concat("-")
                .concat(rDay.getValue().toString()).concat("'");
        }
        
        String update;
        if (newJob) {
            // Formats and inserts the data into an INSERT command
            update = "INSERT INTO EmploymentFX VALUES ("
            + ssn + "," 
            + enStr + "," 
            + fein.getValue() + ",'"
            + hYear.getValue().toString() + "-" + hMonth.getValue().toString() + "-" + hDay.getValue().toString() + "'," 
            + dorStr + "," 
            + credits.getValue() + ")";
        } else {
            // Formats and inserts the data into an UPDATE command
            update = "UPDATE EmploymentFX SET EmployerName = " + enStr
                + ", EmployerFEIN = " + fein.getValue()
                + ", DateOfHire = '" + hYear.getValue().toString() + "-" 
                    + hMonth.getValue().toString() + "-" + hDay.getValue().toString()
                + "', DateOfRetire = " + dorStr
                + ", ServiceCredit = " + credits.getValue()
                + " WHERE SSN = " + ssn 
                + " AND EmployerName = '" + keyEmployer + "';";
        }

        try {
            // Prepares and sends the INSERT command to the database
            PreparedStatement ps = App.connection.prepareStatement(update);
            ps.executeUpdate(update);
    
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
        
    }

    public void setUpdate(String keyEmployer) {            
        // Get the name and code from the EmployerFX table
        try {
            
            this.keyEmployer = keyEmployer;
            ResultSet q = App.stmt.executeQuery("SELECT * FROM EmploymentFX " 
                + "WHERE SSN = '" + ssn 
                + "' AND EmployerName = '" + keyEmployer +"';");

            while (q.next()) {
                // Set the value of the EmployerName
                employerName.setValue(q.getString("EmployerName"));

                // Set the value of the FEIN
                fein.getValueFactory().setValue(q.getInt("EmployerFEIN"));

                Date sqlDoH = q.getDate("DateOfHire");
                LocalDate doh = sqlDoH.toLocalDate();
                hDay.getValueFactory().setValue(doh.getDayOfMonth());
                hMonth.getValueFactory().setValue(doh.getMonth().getValue());
                hYear.getValueFactory().setValue(doh.getYear());

                Date sqlDoR = q.getDate("DateOfRetire");

                retired.setSelected(sqlDoR == null);
                enableRetire();

                if (!retired.isSelected()) {
                    LocalDate dor = sqlDoR.toLocalDate();
                    rDay.getValueFactory().setValue(dor.getDayOfMonth());
                    rMonth.getValueFactory().setValue(dor.getMonth().getValue());
                    rYear.getValueFactory().setValue(dor.getYear());
                }

                
                // Set the value of the FEIN
                credits.getValueFactory().setValue(q.getDouble("ServiceCredit"));
            }
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }

    // enableDoD(): enables and disables the DateOfDeath spinners 
    //              according to the ClickBox status
    public void enableRetire() {

        rMonth.setDisable(retired.isSelected());
        rDay.setDisable(retired.isSelected());
        rYear.setDisable(retired.isSelected());
    }
}