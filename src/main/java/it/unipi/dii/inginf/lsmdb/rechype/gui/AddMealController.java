package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AddMealController extends JSONAdder implements Initializable {

    @FXML private ComboBox mealType;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        mealType.setItems(FXCollections.observableArrayList("Breakfast", "Brunch", "Lunch", "Appetizer", "Dinner"));
    }
}
