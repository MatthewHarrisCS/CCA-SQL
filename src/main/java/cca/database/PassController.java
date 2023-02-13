package cca.database;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PassController {

    @FXML private TextField dbUrl;
    @FXML private TextField username;
    @FXML private PasswordField password;

    // login(): gets the login information from the text fields and passes
    //          them to the main app for connecting to the database
    public void login() {
        // sqlUrl attaches the url from the text field with the required
        // fields to construct the full SQL url
        App.sqlUrl = "jdbc:mysql://".concat(dbUrl.getText()).concat("?useSSL=false");
        App.sqlUser = username.getText();
        App.sqlPassword = password.getText();
    }

}
