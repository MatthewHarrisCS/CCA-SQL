package cca.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBController implements Initializable{

    @FXML private TableView<Person> table;
    @FXML private TableColumn<Person, Integer> ssn;
    @FXML private TableColumn<Person, String> name;
    @FXML private TableColumn<Person, String> gender;
    @FXML private TableColumn<Person, Date> dateOfBirth;
    @FXML private TableColumn<Person, Date> dateOfDeath;
    @FXML private ChoiceBox<String> employerList;
    @FXML private CheckBox livingCheck;
    @FXML private Button viewJobsButton;
    @FXML private Button saveButton;
    @FXML private Button personUpdateButton;
    @FXML private Button personDeleteButton;
    @FXML private Button employerUpdateButton;
    @FXML private Button employerDeleteButton;
    private String query = "SELECT * FROM PersonFX;";
    private String switchResult;

    ObservableList<Person> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Gets the employer name list and adds them to the filter menu
        employerList.setItems(App.employerList);
        setEmployerControls();

        // Code to build the columns of the table
        ssn.setCellValueFactory(new PropertyValueFactory<Person, Integer>("ssn"));
        name.setCellValueFactory(new PropertyValueFactory<Person, String>("name"));
        gender.setCellValueFactory(new PropertyValueFactory<Person, String>("gender"));
        dateOfBirth.setCellValueFactory(new PropertyValueFactory<Person, Date>("dateOfBirth"));
        dateOfDeath.setCellValueFactory(new PropertyValueFactory<Person, Date>("dateOfDeath"));

        getDatabase();
    }

    // getDatabase(): queries the database for the PersonFX table and inserts
    //                them into the ObservableList to display in the table
    @FXML
    private void getDatabase() {

        // Clears the list of any previous entries
        list.clear();

        // Populates the table list with data from the SQL database
        try {
            // Queries the PersonFX table from the SQL database
            // and formats them for the JavaFX table
            ResultSet q = App.stmt.executeQuery(query);
            while (q.next()) {
                list.add(
                    new Person(q.getInt("SSN"), 
                    q.getString("FirstName").concat(" ")
                        .concat(q.getString("LastName")), 
                    q.getString("Gender"), 
                    q.getString("DateOfBirth"), 
                    q.getString("DateOfDeath")));
            }

        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }

        // Attach the list to the table
        table.setItems(list);

        // Disable Update/Remove buttons if no people in database
        viewJobsButton.setDisable(list.isEmpty());
        personUpdateButton.setDisable(list.isEmpty());
        personDeleteButton.setDisable(list.isEmpty());
    }

    private Boolean getDeleteView(String source, int ssn, String employerName) {

        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("delete.fxml")));
            DialogPane deleteDialog = fxmlLoader.load();

            // Initialize the dialog box
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(deleteDialog);
            dialog.setTitle("Remove " + source);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));

            // Get the controller and pass the variables to it
            DeleteController controller = fxmlLoader.getController();
            controller.switchFunction(source, ssn, employerName);

            // Access the dialog box and return the value of the button
            dialog.showAndWait().filter(ButtonType.OK::equals)
            .ifPresent(Button -> switchResult = controller.getChoice());
            ;
            return (dialog.getResult() == ButtonType.OK);
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
            return false;
        }
    }

    // getPersonView(): Calls the PersonEntry window to insert
    //                   a new entry into the PersonFX table
    private void getPersonView(Boolean newPerson) {

        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("personEntry.fxml")));
            DialogPane entryDialog = fxmlLoader.load();

            // Initialize the dialog box and get access to the controller
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(entryDialog);
            PersonEntryController controller = fxmlLoader.getController();
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));

            System.out.println(newPerson);
            if (newPerson) {
                // Set the window for add mode
                dialog.setTitle("Add Person");

            } else {
                // Set the window for update mode
                Person person = list.get(table.getSelectionModel()
                    .getFocusedIndex());
                controller.setUpdate(person.getSsn());
                dialog.setTitle("Update Person");
            }
            
            // Access the dialog box, and update the PersonFX database if
            // OK is selected, otherwise nothing if CANCEL or window close
            dialog.showAndWait().filter(ButtonType.OK::equals)
                .ifPresent(Button -> controller.setNewPerson(newPerson));

            // Refresh the database and reset the employer list
            getDatabase();
            setEmployerControls();
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
        }
    }

    @FXML
    private void getPersonAdd() {
        getPersonView(true);
    }

    @FXML
    public void getPersonUpdate() {
        getPersonView(false);
    }

    @FXML
    public void getPersonDelete() {
        Person view = list.get(table.getSelectionModel().getFocusedIndex());
        if(!getDeleteView("Person", view.ssn, null)) {
            return;
        }

        String delete = "DELETE FROM PersonFX WHERE SSN = " + view.ssn;

        try {
            // Prepares and sends the INSERT command to the database
            PreparedStatement ps = App.connection.prepareStatement(delete);
            ps.executeUpdate(delete);

            getDatabase();
    
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }

    // getEmployerView(): Calls the Employer window to insert or
    //                    update an entry into the EmployerFX table
    
    private void getEmployerView(Boolean newEntry) {

        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("employerEntry.fxml")));
            DialogPane entryDialog = fxmlLoader.load();

            // Initialize the dialog box and get access to the controller
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(entryDialog);
            EmpEntryController controller = fxmlLoader.getController();
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));

            System.out.println(newEntry);
            if (newEntry) {
                // Access the dialog box, and add the new employer to the 
                // database if OK is selected, otherwise nothing if CANCEL 
                // or window close
                dialog.setTitle("Add Employer");
                dialog.showAndWait().filter(ButtonType.OK::equals)
                    .ifPresent(Button -> controller.setNewEmployer());
            } else {
                // Set the window for update mode
                controller.setUpdate();
                dialog.setTitle("Update Employer");
                
                // Access the dialog box, and update the employer to the 
                // database if OK is selected, otherwise nothing if CANCEL 
                // or window close
                dialog.showAndWait().filter(ButtonType.OK::equals)
                    .ifPresent(Button -> controller.updateEmployer());
            }

            // Update the employer name list and set the initial value
            App.setNameList();
            setEmployerControls();
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
        }
    }

    @FXML 
    private void getEmployerAdd() {
        getEmployerView(true);
    }

    @FXML 
    private void getEmployerUpdate() {
        getEmployerView(false);
    }

    @FXML 
    private void getEmployerDelete() {
    
    String keyEmployer;
        if(!getDeleteView("Employer - Select An Employer", 0, null)
            || switchResult == null) {
            
            return;

        } else {            
            
            keyEmployer = switchResult;

            if(!getDeleteView("Employer", 0, keyEmployer)) {
                return;
            }
        }

        System.out.println(keyEmployer);
        String delete = "DELETE FROM EmployerFX WHERE EmployerName = '" + keyEmployer+ "';";
        System.out.println(delete);

        try {
            // Prepares and sends the INSERT command to the database
            PreparedStatement ps = App.connection.prepareStatement(delete);
            ps.executeUpdate(delete);

            App.setNameList();
            setEmployerControls();
    
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }    

    // getJobView(): Calls the JobView window to view all jobs
    //               held by a person in the EmploymentFX table
    @FXML
    private void getJobView() {

        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("jobView.fxml")));
            DialogPane jobDialog = fxmlLoader.load();

            // Initialize the dialog box
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(jobDialog);
            dialog.setTitle("View Job");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));

            // Get the selected person from the table and get their data
            // to add to the JobView controller 
            Person view = list.get(table.getSelectionModel().getFocusedIndex());
            JobViewController controller = fxmlLoader.getController();
            controller.setPerson(view.getSsn());
            dialog.setTitle("View Jobs for " + view.getName());

            // Access the dialog box
            dialog.showAndWait();

            // Refresh the database and reset the employer list
            getDatabase();
            setEmployerControls();
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
        }
    }

    // filterPeople(): Adjusts the default database query to access
    //                 specific people in the PersonFX table
    @FXML
    private void filterPeople() {

        // Set initial values for the parameters
        String employerFilter = "";
        String livingFilter = "";

        // Starts as WHERE to join the SELECT statement to the first parameter
        // and is updated to AND when a parameter is set to join parameters
        String join = " WHERE";

        // Set the initial query
        query = "SELECT * FROM PersonFX";

        // If no value exists for the employer list, skip this step
        if(employerList.getValue() != null) {

            // If the value isn't a default value, 
            // initialize the employerFilter parameter
            if(!employerList.getValue().equals("(All Employers)") &&
            !employerList.getValue().equals("(Unemployed)")) {
                
                // Get every SSN for the company in the EmploymentFX table
                employerFilter = " WHERE SSN IN (SELECT DISTINCT SSN FROM " + 
                    "EmploymentFX WHERE EmployerName = '" 
                    + employerList.getValue() + "')";
                join = " AND";

            // If the value is (Unemployed), ignores all SSNs in the table
            } else if(employerList.getValue().equals("(Unemployed)")) {
                    
                employerFilter = " WHERE SSN NOT IN "
                    + "(SELECT DISTINCT SSN FROM EmploymentFX)";
                join = " AND";
            }
        }

        // if the living check is set, ignores people with set DateOfDeaths
        if (livingCheck.isSelected()) {
            livingFilter = join + " DateOfDeath IS NULL";
        }

        // Attaches the parameters to the base query and appends a semicolon
        query = query + employerFilter + livingFilter + ";";

        // Calls the getDatabase() function to refresh with the new query
        getDatabase();
    }

    private void setEmployerControls() {
        Boolean isEmpty = App.employerList.size() <= 2;
        employerList.setDisable(isEmpty);
        employerUpdateButton.setDisable(isEmpty);
        employerDeleteButton.setDisable(isEmpty);

        if(isEmpty) {
            employerList.setValue("(Unemployed)");
        } else {
            employerList.setValue("(All Employers)");
        }
    }

    @FXML
    private void saveCSV() {
        System.out.println("Saving...");

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Database to CSV");
        fc.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
        File home = new File(System.getProperty("user.dir"));
        fc.setInitialDirectory(home);
        File csv = fc.showSaveDialog(saveButton.getScene().getWindow());

        // TO DO test file extension
        if (csv == null) {
            return;
        }
        
        try {
            csv.createNewFile();
            System.out.println(csv.toString() + " " + csv.canWrite() + " " + csv.canRead());
            FileWriter w = new FileWriter(csv.toString());
            w.write("SSN,LAST NAME,FIRST NAME,GENDER,DATE OF BIRTH,DATE OF DEATH\n");
            ResultSet q = App.stmt.executeQuery(query);
            while (q.next()) {
                w.write(
                    q.getInt("SSN") + "," + 
                    q.getString("LastName")+ "," + 
                    q.getString("FirstName") + "," + 
                    q.getString("Gender")+ "," + 
                    q.getString("DateOfBirth")+ "," + 
                    q.getString("DateOfDeath") + "\n");
            }
            w.close();

            System.out.println("Saved");

        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("SAVE ERROR: " + e.getMessage());
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }

    }

    @FXML
    private void onDoubleClick(MouseEvent event) {
        if(event.getClickCount() == 2 && !this.table.getItems().isEmpty()) {
            getJobView();
        }
    }

    
    @FXML
    private void onEnter(KeyEvent event) {
        if(table.isFocused() && event.getCode().equals(KeyCode.ENTER) 
                             && !this.table.getItems().isEmpty()) {
            getJobView();
        }
    }
}
