package cca.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.time.Period;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class JobViewController implements Initializable {

    @FXML private TableView<Employment> table;
    @FXML private TableColumn<Employment, String> employerName;
    @FXML private TableColumn<Employment, Integer> employerFEIN;
    @FXML private TableColumn<Employment, Date> dateOfHire;
    @FXML private TableColumn<Employment, Date> dateOfRetire;
    @FXML private TableColumn<Employment, Double> serviceCredit;
    @FXML private Label nameField;
    @FXML private Label nameField2;
    @FXML private Label genderField;
    @FXML private Label deceasedField;
    @FXML private Label ageField;
    @FXML private Label jobsField;
    @FXML private Label creditField;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;
    @FXML private Button updatePersonButton;
    @FXML private Button deletePersonButton;
    @FXML private HBox jobButtonBox;
    @FXML private HBox nameBox;
    private int ssn;
    
    ObservableList<Employment> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Code to build the columns of the table
        employerName.setCellValueFactory(new PropertyValueFactory<Employment, String>("employerName"));
        employerFEIN.setCellValueFactory(new PropertyValueFactory<Employment, Integer>("employerFEIN"));
        dateOfHire.setCellValueFactory(new PropertyValueFactory<Employment, Date>("dateOfHire"));
        dateOfRetire.setCellValueFactory(new PropertyValueFactory<Employment, Date>("dateOfRetire"));
        serviceCredit.setCellValueFactory(new PropertyValueFactory<Employment, Double>("serviceCredit"));

        table.setItems(list);
        
    }

    public void setPerson(int ssn) {

        this.ssn = ssn;

        try {
            ResultSet q = App.stmt.executeQuery("SELECT * FROM PersonFX WHERE SSN = " + ssn + ";");

            while(q.next()) {

                nameField.setText(q.getString("FirstName") + " " + q.getString("LastName"));

                String gender = q.getString("Gender");
                Date dob = q.getDate("dateOfBirth");
                Date dod = q.getDate("dateOfDeath");

                if (gender.equals("M")) {
                    genderField.setText("Male");
                } else if (gender.equals("F")) {
                    genderField.setText("Female");
                } else {
                    genderField.setText("Other");
                }

                int age;
                if (dod != null) {
                    age = Period.between(dob.toLocalDate(), dod.toLocalDate()).getYears();
                    deceasedField.setText(dod.toString());
                } else {
                    age = Period.between(dob.toLocalDate(), LocalDate.now()).getYears();
                    deceasedField.setText("N/A");
                }
                ageField.setText(age + "");
                nameField2.setText(nameField.getText());
            }

        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }

        setJobs();
    }

    public void setJobs() {
        try {

            int jobsWorked = 0;
            double serviceCredit = 0.0;
            list.clear();

            ResultSet q = App.stmt.executeQuery("SELECT * FROM EmploymentFX WHERE SSN = " + ssn + ";");

            while(q.next()) {
                list.add(
                    new Employment(q.getInt("SSN"),
                    q.getString("EmployerName"),
                    q.getInt("EmployerFEIN"),
                    q.getString("DateOfHire"),
                    q.getString("DateOfRetire"),
                    q.getDouble("ServiceCredit")));

                    jobsWorked++;
                    serviceCredit += q.getDouble("ServiceCredit");
            }

            table.setItems(list);

            jobsField.setText(jobsWorked + "");
            creditField.setText(serviceCredit + "");

            updateButton.setDisable(list.isEmpty());
            deleteButton.setDisable(list.isEmpty());

        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }
    }

        // getJobEntry(): Calls the JobEntry window to insert a
    //                new entry into the EmploymentFX table
    private void getJobEntry(Boolean newJob) {

        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("jobEntry.fxml")));
            DialogPane entryDialog = fxmlLoader.load();

            // Initialize the dialog box
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(entryDialog);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));

            // Get the selected person from the table and get their data
            // to add to the JobEntry controller 
            JobEntryController controller = fxmlLoader.getController();
            controller.setName(ssn);

            if (newJob) {
                // Set the window for add mode
                dialog.setTitle("Add Job for " + nameField.getText());

            } else {
                // Set the window for update mode
                Employment job = list.get(table.getSelectionModel()
                    .getFocusedIndex());
                controller.setUpdate(job.getEmployerName());
                dialog.setTitle("Update " + job.getEmployerName() 
                    + " for " + nameField.getText());
            }

            // Access the dialog box, and add the new job to the database
            // if OK is selected, otherwise nothing if CANCEL or window close
            dialog.showAndWait().filter(ButtonType.OK::equals)
                .ifPresent(Button -> controller.setJob(newJob));

            // Refresh the job table
            setJobs();
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
        }
    }

    @FXML
    private void getJobAdd() {
        getJobEntry(true);
    }
    @FXML
    private void getJobUpdate() {
        getJobEntry(false);
        
    }

    private void getDelete(Boolean isJob) {

        try {
            // Access the .fxml file for the dialog box
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation((App.class.getResource("delete.fxml")));
            DialogPane deleteDialog = fxmlLoader.load();

            // Initialize the dialog box and get the controller
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(deleteDialog);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(App.class.getResourceAsStream("LC.png")));
            DeleteController controller = fxmlLoader.getController();

            if(isJob) {
                Employment job = list.get(table.getSelectionModel()
                    .getFocusedIndex());
                controller.switchFunction("Job", ssn, job.getEmployerName());
                dialog.setTitle("Remove " + job.getEmployerName() 
                    + " for " + nameField.getText());

                // Access the dialog box and return the value of the button
                dialog.showAndWait().filter(ButtonType.OK::equals)
                    .ifPresent(Button -> deleteJob(job.getEmployerName()));
            } else {
                
                controller.switchFunction("Person", ssn, null);
                dialog.setTitle("Remove " + nameField.getText());

                // Access the dialog box and return the value of the button
                dialog.showAndWait().filter(ButtonType.OK::equals)
                    .ifPresent(Button -> deletePerson(ssn));
            }
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
        }
    }

    @FXML
    private void getJobDelete() {
        getDelete(true);
    }

    @FXML
    private void getPersonDelete() {
        getDelete(false);
    }

    
    private void deleteJob(String employerName) {
        String delete = "DELETE FROM EmploymentFX WHERE SSN = " + ssn 
            + " AND EmployerName = '" + employerName + "';";

        try {
            // Prepares and sends the DELETE command to the database
            PreparedStatement ps = App.connection.prepareStatement(delete);
            ps.executeUpdate(delete);

            setJobs();
    
        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }
    }

    private void deletePerson(int ssn) {
        String delete = "DELETE FROM PersonFX WHERE SSN = " + ssn + ";";

        try {
            // Prepares and sends the DELETE command to the database
            PreparedStatement ps = App.connection.prepareStatement(delete);
            ps.executeUpdate(delete);

        } catch (SQLException e) {
            // If error, prints message to the command line
            App.errorMsg(e);
        }

        // Close the jobView window
        Stage stage = (Stage) deletePersonButton.getScene().getWindow();
        stage.close();
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
            w.write("EMPLOYER NAME,EMPLOYER FEIN,DATE OF HIRE,DATE OF RETIRE,SERVICE CREDITS\n");

            ResultSet q = App.stmt.executeQuery("SELECT * FROM EmploymentFX WHERE SSN = " + ssn + ";");

            while(q.next()) {
                w.write(
                    q.getString("EmployerName") + "," +
                    q.getInt("EmployerFEIN") + "," +
                    q.getString("DateOfHire") + "," +
                    q.getString("DateOfRetire") + "," +
                    q.getDouble("ServiceCredit") + "\n");
            }
            w.close();

            System.out.println("Saved");

        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("SAVE ERROR: " + e.getMessage());
        } catch (SQLException e) {
            // If SQL error, calls the error window
            App.errorMsg(e);
        }

    }

    @FXML
    private void getPersonUpdate() {
        
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

            controller.setUpdate(ssn);
            dialog.setTitle("Update Person");
            
            // Access the dialog box, and update the PersonFX database if
            // OK is selected, otherwise nothing if CANCEL or window close
            dialog.showAndWait().filter(ButtonType.OK::equals)
                .ifPresent(Button -> {
                    int newSsn = controller.ssn.getValue();
                    controller.setNewPerson(false);
                    setPerson(newSsn);
                    setJobs();
                });
            
        } catch (IOException e) {  
            // If error, print an error message to the terminal
            System.out.println("DIALOG BOX ERROR: " + e.getMessage());
        }
    }
}