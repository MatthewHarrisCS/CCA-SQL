package cca.database;

import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class PersonEntryController implements Initializable {

    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private Label fnLimit;
    @FXML private Label lnLimit;
    @FXML private ToggleGroup genderToggle;
    @FXML public Spinner<Integer> ssn;
    @FXML private Spinner<Integer> bMonth;
    @FXML private Spinner<Integer> bDay;
    @FXML private Spinner<Integer> bYear;
    @FXML private Spinner<Integer> dMonth;
    @FXML private Spinner<Integer> dDay;
    @FXML private Spinner<Integer> dYear;
    @FXML private CheckBox alive;
    private int keySsn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Set the spinner ranges and default values
        ssn.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1000000, 999999999, 11111111));
        bMonth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12));
        bDay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31));
        bYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2999, 1999));
        dMonth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12));
        dDay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31));
        dYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2999, 1999));
        
    }

    // enableDoD(): enables and disables the DateOfDeath spinners 
    //              according to the ClickBox status
    public void enableDoD() {

        dMonth.setDisable(alive.isSelected());
        dDay.setDisable(alive.isSelected());
        dYear.setDisable(alive.isSelected());
    }

    // setNewPerson(): takes entered data and inserts it into the PersonFX table
    public void setNewPerson(Boolean newPerson) {
        
        // Format the names into strings for the INSERT command or
        // sets the values null if the text fields are empty
        String lnStr;
        String fnStr;
        if (lastName.getText().equals("")) {
            lnStr = "NULL";
        } else {
            lnStr = "'" + lastName.getText() + "'";
        }

        if (firstName.getText().equals("")) {
            fnStr = "NULL";
        } else {
            fnStr = "'" + firstName.getText() + "'";
        }

        // Gets the selected value from the Gender radio buttons and selects
        // the correct ENUM value to enter into the INSERT command
        String genderStr;
        RadioButton selected = (RadioButton) genderToggle.getSelectedToggle();
        String genderSelect = selected.getText();

        if (genderSelect.equals("Male")) {
            genderStr = "'M'";
        } else if (genderSelect.equals("Female")) {
            genderStr = "'F'";
        } else {
            genderStr = "'X'";
        }

        // Sets the DateOfDeath string to an insertable NULL value if Not
        // Applicable is selected, otherwise formats the date into the SQL
        // date format to enter into the INSERT command
        String dodStr;
        if (alive.isSelected()) {
            dodStr = "NULL";
        } else {
            dodStr = "'".concat(dYear.getValue().toString()).concat("-")
                .concat(dMonth.getValue().toString()).concat("-")
                .concat(dDay.getValue().toString()).concat("'");
        }
        

        // Uses newPerson to decide between an INSERT or UPDATE statement
        String update;
        if (newPerson) {
            // Formats and inserts the data into an INSERT command
            update = "INSERT INTO PersonFX VALUES ("
                + ssn.getValue() + "," 
                + lnStr + "," 
                + fnStr + ","
                + genderStr + ",'"
                + bYear.getValue().toString() + "-" + bMonth.getValue().toString() 
                    + "-" + bDay.getValue().toString() + "'," 
                + dodStr + ")";
        } else {
            // Formats and inserts the data into an UPDATE command
            update = "UPDATE PersonFX SET SSN = " + ssn.getValue()
                + ", LastName = " + lnStr
                + ", FirstName = " + fnStr
                + ", Gender = " + genderStr
                + ", DateOfBirth = '" + bYear.getValue().toString() + "-" 
                    + bMonth.getValue().toString() + "-" + bDay.getValue().toString()
                + "', DateOfDeath = " + dodStr
                + " WHERE SSN = " + keySsn + ";";
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

    public void setUpdate(int keySsn) {            
        // Get the name and code from the EmployerFX table
        try {
            ResultSet q = App.stmt.executeQuery("SELECT * FROM PersonFX " 
                + "WHERE SSN = '" + keySsn + "';");
                this.keySsn = keySsn;

            while (q.next()) {
                // Set the names and name limit counters
                lastName.setText(q.getString("LastName"));
                setLnLimit();
                firstName.setText(q.getString("FirstName"));
                setFnLimit();

                // Set the value of the SSN
                ssn.getValueFactory().setValue(q.getInt("SSN"));

                // Set the gender toggle to female if needed, otherwise male
                if (q.getString("Gender").equals("F")) {
                   genderToggle.selectToggle(genderToggle.getToggles().get(1));
                }

                Date sqlDoB = q.getDate("DateOfBirth");
                LocalDate dob = sqlDoB.toLocalDate();
                bDay.getValueFactory().setValue(dob.getDayOfMonth());
                bMonth.getValueFactory().setValue(dob.getMonth().getValue());
                bYear.getValueFactory().setValue(dob.getYear());

                Date sqlDoD = q.getDate("DateOfDeath");

                alive.setSelected(sqlDoD == null);
                enableDoD();

                if (!alive.isSelected()) {
                    LocalDate dod = sqlDoD.toLocalDate();
                    dDay.getValueFactory().setValue(dod.getDayOfMonth());
                    dMonth.getValueFactory().setValue(dod.getMonth().getValue());
                    dYear.getValueFactory().setValue(dod.getYear());
                    
                }
                
            }
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }

    // setFnLimit(): Counter function for the character limit
    @FXML 
    private void setFnLimit() {
        fnLimit.setText(firstName.getLength() + "");
    }

    // setLnLimit(): Counter function for the character limit
    @FXML 
    private void setLnLimit() {
        lnLimit.setText(lastName.getLength() + "");
    }
}
