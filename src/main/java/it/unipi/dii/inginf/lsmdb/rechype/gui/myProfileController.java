package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class myProfileController  extends JSONAdder implements Initializable {

    @FXML private Button addIngredientBtn;
    @FXML private Button createMealBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addIngredientBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("IngredientSearch", new JSONObject());

            }
        });

        createMealBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("AddMeal", new JSONObject());

            }
        });
    }
}
