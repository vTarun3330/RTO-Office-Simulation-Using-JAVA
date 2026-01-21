module com.rto {
  requires transitive javafx.controls;
  requires transitive javafx.fxml;
  requires java.sql;

  opens com.rto to javafx.fxml;
  opens com.rto.controller to javafx.fxml;
  opens com.rto.model to javafx.fxml;

  exports com.rto;
  exports com.rto.controller;
  exports com.rto.model;
  exports com.rto.service;
  exports com.rto.patterns;
}
