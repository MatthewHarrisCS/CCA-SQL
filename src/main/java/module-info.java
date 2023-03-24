module cca.database {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires transitive java.sql;

    opens cca.database to javafx.fxml;
    exports cca.database;
}
