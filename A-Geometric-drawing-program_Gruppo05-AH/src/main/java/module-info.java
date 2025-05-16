module com.geometricdrawing.ageometricdrawingprogram_gruppo05ah {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.apache.pdfbox;


    opens com.geometricdrawing to javafx.fxml;
    exports com.geometricdrawing;
}