package cca.database;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class ErrorController implements Initializable{

    @FXML private Label err;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        err.setText(App.err);
    }
}
