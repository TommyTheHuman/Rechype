package it.unipi.dii.inginf.lsmdb.rechype.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class myProfileController implements Initializable {

    @FXML private Button addIngredientBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addIngredientBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.changeScene("IngredientSearch");

            }
        });
    }
}
