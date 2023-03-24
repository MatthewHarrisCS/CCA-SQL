package cca.database;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PassController implements Initializable {

    @FXML private TextField dbUrl;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private CheckBox remember;

    private Preferences prefs = Preferences.userRoot();
    private final String DB_VAL = "CCA_SQL_DB_VAL"; 
    private final String UN_VAL = "CCA_SQL_UN_VAL";
    private final String PW_VAL = "CCA_SQL_PW_VAL";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        // Get the preferences for remembered login (if they exist)
        dbUrl.setText(prefs.get(DB_VAL, "localhost:3306/db"));
        username.setText(prefs.get(UN_VAL, "root"));
        password.setText(prefs.get(PW_VAL, ""));
    }


    // login(): gets the login information from the text fields and passes
    //          them to the main app for connecting to the database
    public void login() {

        // sqlUrl attaches the url from the text field with the required
        // fields to construct the full SQL url
        App.sqlUrl = "jdbc:mysql://".concat(dbUrl.getText()).concat("?useSSL=false");
        App.sqlUser = username.getText();
        App.sqlPassword = password.getText();

        // If 'Remember Login?' selected, save the info as user preferences
        if(remember.isSelected()) {
            prefs.put(DB_VAL, dbUrl.getText());
            prefs.put(UN_VAL, username.getText());
            prefs.put(PW_VAL, password.getText());
        // Else remove all user preferences
        } else {
            prefs.remove(DB_VAL);
            prefs.remove(UN_VAL);
            prefs.remove(PW_VAL);
        }
    }

}
