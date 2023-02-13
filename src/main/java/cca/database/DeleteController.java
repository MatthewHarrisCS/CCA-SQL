package cca.database;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DeleteController implements Initializable {

    @FXML private ChoiceBox<String> deleteEmpList;
    @FXML private Text text1;
    @FXML private Text deleteText;
    @FXML private Text text2;
    @FXML private FlowPane listFlow;
    @FXML private TextFlow textFlow;
    private String employerName;
    private String firstName;
    private String lastName;
    private String empChoice;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        FilteredList<String> employerList = new FilteredList<>(App.employerList, s -> true);
        employerList.setPredicate(i -> i.charAt(0) != '(');
        deleteEmpList.setItems(employerList);

    }

    public void switchFunction(String source, int srcSsn, String srcEmployerName) {
        
        String query = "";
        ResultSet q;

        try {
            switch(source) {
                case "Person":
                    query = "SELECT * FROM PersonFX WHERE SSN = '"
                        + srcSsn + "';";
                    
                    q = App.stmt.executeQuery(query);
                    while (q.next()) {
                        firstName = q.getString("FirstName");
                        lastName = q.getString("LastName");
                    }

                    deleteText.setText(firstName + " " + lastName);
                    break;

                case "Employer":
                    query = "SELECT * FROM EmployerFX WHERE EmployerName = '"
                        + srcEmployerName + "';";

                    q = App.stmt.executeQuery(query);
                    while (q.next()) {
                        employerName = q.getString("EmployerName");
                    }

                    deleteText.setText(employerName);
                    break;

                case "Job":
                    query = "SELECT * FROM EmploymentFX NATURAL INNER JOIN "
                        + "PersonFX WHERE EmployerName = '" + srcEmployerName 
                        + "' AND SSN = '" + srcSsn + "';";
                        
                    q = App.stmt.executeQuery(query);
                    while (q.next()) {
                        firstName = q.getString("FirstName");
                        lastName = q.getString("LastName");
                        employerName = q.getString("EmployerName");
                    }

                    deleteText.setText(firstName + " " + lastName 
                        + "'s job with " + employerName);
                    break;

                case "Employer - Select An Employer":
                    listFlow.setMaxHeight(42.0);
                    listFlow.setVisible(true);

                    textFlow.setMaxHeight(0.0);
                    textFlow.setVisible(false);

                    text1.setFont(Font.font(0));
                    deleteText.setFont(Font.font(0));
                    text2.setFont(Font.font(0));

                    textFlow.setMaxHeight(0.0);
                    textFlow.setVisible(false);

                default:
                    return;
            }

        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }

        return;
    }

    @FXML
    private void setChoice() {
        empChoice = deleteEmpList.getValue();
    }

    public String getChoice() {
        return empChoice;
    }
}
