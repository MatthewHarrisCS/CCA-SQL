package cca.database;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static Connection connection;
    public static Statement stmt;
    public static String err = "";
    public static String sqlUrl;
    public static String sqlUser;
    public static String sqlPassword;
    public static ObservableList<String> employerList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) throws IOException {

        try {
            // Log in to the database or close on cancel
            if(!sqlConnect()) {
                Platform.exit();
                return;
            }

            // Display the scene
            Scene scene;
            scene = new Scene(loadFXML("db"));
            stage.setScene(scene);
            stage.setTitle("JavaFX Database Prototype");
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));
            stage.show();
        } catch (SQLException e) {
            // If SQL exception, display error window and close the application
            errorMsg(e);
            Platform.exit();
        }
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // errorMsg(SQLException e)
    public static void errorMsg(SQLException e) {
        
        try {
            // Sets the internal error message
            err = "SQL ERROR:\n" + e.getMessage();

            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("error.fxml")));
            DialogPane errDialog = fxmlLoader.load();

            // Initialize the dialog box
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(errDialog);
            dialog.setTitle("Database Error");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));

            // Display the dialog box
            dialog.showAndWait();
            
        } catch (IOException e2) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e2.getMessage());
        }
    }

    // sqlConnect(): creates the password dialog box and uses the information
    //             : entered to connect to the MySQL database
    private static Boolean sqlConnect() throws SQLException {
        
        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("pass.fxml")));
            DialogPane passDialog = fxmlLoader.load();

            // Initialize the dialog box and get access to the controller
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(passDialog);
            dialog.setTitle("Enter Password");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));
            PassController controller = fxmlLoader.getController();

            // Display the dialog box and call the login function on OK
            dialog.showAndWait();

            if(dialog.getResult().equals(ButtonType.OK)) {

                // Call the login function on OK
                controller.login();

                // Use the login info from login() to connect to the database
                connection = DriverManager.getConnection(
                    sqlUrl, sqlUser, sqlPassword);
                stmt = connection.createStatement();

                // Create the list of employer names
                setNameList();
                return true;
            }
            
        } catch (IOException e2) {  
            // If IO error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e2.getMessage());
        }

        return false;
    }

    // setNameList(): creates the employer name list for drop down menus
    public static void setNameList() {
        try {
            // Clears the list of previous entries and add the default entry
            employerList.clear();
            employerList.add("(All Employers)");

            // Queries the EmployerFX table from the SQL database
            // and collects all of the employer names
            ResultSet q = App.stmt.executeQuery("SELECT DISTINCT EmployerName FROM EmployerFX;");
            while (q.next()) {
                employerList.add(q.getString("EmployerName"));
            }
            employerList.add("(Unemployed)");

        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}