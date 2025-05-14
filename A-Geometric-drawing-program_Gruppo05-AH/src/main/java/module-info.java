module com.geometricdrawing.ageometricdrawingprogram_gruppo05ah {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.geometricdrawing.ageometricdrawingprogram_gruppo05ah to javafx.fxml;
    exports com.geometricdrawing.ageometricdrawingprogram_gruppo05ah;
}