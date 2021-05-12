package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class RecipePageController extends JSONAdder implements Initializable {

    @FXML private Text authorLabel;
    @FXML private Text Name;
    @FXML private Text Description;
    @FXML private Text Kcal;
    @FXML private Text WeightPerServing;
    @FXML private Text ReadyInMinutes;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
