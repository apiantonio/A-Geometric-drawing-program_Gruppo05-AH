module com.geometricdrawing.ageometricdrawingprogram_gruppo05ah {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.apache.pdfbox;


    opens com.geometricdrawing to javafx.fxml;
    exports com.geometricdrawing.controller;
    exports com.geometricdrawing.model;
    exports com.geometricdrawing.command;
    exports com.geometricdrawing.factory;
    exports com.geometricdrawing.strategy;
    exports com.geometricdrawing.mousehandler;
    exports com.geometricdrawing.decorator;
    opens com.geometricdrawing.controller to javafx.fxml;
    exports com.geometricdrawing;
}