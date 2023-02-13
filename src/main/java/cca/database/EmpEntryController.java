package cca.database;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class EmpEntryController implements Initializable {

    @FXML private Label charLimit;
    @FXML private TextField employerName;
    @FXML private ChoiceBox<String> retirementCode;
    @FXML private FlowPane updateFlow;
    @FXML private ChoiceBox<String> updateEmpList;
    private ObservableList<String> codeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeList.add("DSRS");
        codeList.add("EMRS");
        codeList.add("JRS");
        codeList.add("MPFRS");
        codeList.add("NRPRS");
        codeList.add("PERS");
        codeList.add("SPTA");
        codeList.add("SPTB");
        codeList.add("TRS");
        retirementCode.setItems(codeList);

        // Sets the update choice box nonvisible until called by setUpdate()
        updateFlow.setVisible(false);
        updateFlow.setMaxHeight(0.0);
    }

    public void setUpdate() {
        
        // Sets the update choice box visible
        updateFlow.setVisible(true);
        updateFlow.setMaxHeight(44.0);

        FilteredList<String> employerList = new FilteredList<>(App.employerList, s -> true);
        employerList.setPredicate(i -> i.charAt(0) != '(');
        updateEmpList.setItems(employerList);

        System.out.println(updateEmpList.getValue());
    }

    @FXML
    private void chooseUpdate() {

        try {
            // Get the name and code from the EmployerFX table
            ResultSet q = App.stmt.executeQuery("SELECT * FROM EmployerFX " 
                + "WHERE EmployerName = '" + updateEmpList.getValue() + "';");

            while (q.next()) {
                employerName.setText(q.getString("EmployerName"));
                retirementCode.setValue(q.getString("RetirementCode"));
            }

            // Set the fields to the selected employer
            setCharLimit();
        
        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }
    }

    public void setNewEmployer() {
        try {
            String enStr = employerName.getText().toUpperCase();
            if (enStr.equals("")) {
                enStr = "NULL";
            } else {
                enStr = "'" + enStr + "'";
            }

            // Formats and inserts the data into an INSERT command
            String insert = "INSERT INTO EMPLOYERFX VALUES ("
            + enStr + ",'" 
            + retirementCode.getValue() + "')";

            System.out.println(insert);

            // Prepares and sends the INSERT command to the database
            PreparedStatement ps = App.connection.prepareStatement(insert);
            ps.executeUpdate(insert);
    
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }

    
    public void updateEmployer() {
        
        if(updateEmpList.getValue() == null) {
            return;
        }

        try {
            String enStr = employerName.getText().toUpperCase();
            if (enStr.equals("")) {
                enStr = "NULL";
            } else {
                enStr = "'" + enStr + "'";
            }

            // Formats and inserts the data into an UPDATE command
            String update = "UPDATE EmployerFX SET EmployerName = '" + employerName.getText()
                + "', RetirementCode = '" + retirementCode.getValue()
                + "' WHERE EmployerName = '" + updateEmpList.getValue() + "';";

            System.out.println(update);

            // Prepares and sends the UPDATE command to the database
            PreparedStatement ps = App.connection.prepareStatement(update);
            ps.executeUpdate(update);
    
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }

    // setCharLimit(): Counter function for the character limit
    @FXML 
    private void setCharLimit() {
        charLimit.setText(employerName.getLength() + "");
    }
}