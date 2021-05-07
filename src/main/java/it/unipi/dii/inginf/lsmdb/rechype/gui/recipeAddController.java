package it.unipi.dii.inginf.lsmdb.rechype.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class recipeAddController implements Initializable {

    @FXML private Button addIngredientButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        addIngredientButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("IngredientSearch");

            }
        });


    }
}
