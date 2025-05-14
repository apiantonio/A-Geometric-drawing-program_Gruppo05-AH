module com.geometricdrawing.ageometricdrawingprogram_gruppo05ah {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.geometricdrawing to javafx.fxml;
    exports com.geometricdrawing;
}