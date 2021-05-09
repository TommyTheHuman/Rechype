package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class userProfileController extends JSONAdder implements Initializable {

    @FXML private Text userText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //userText.setText(jsonParameters.get("id").toString());


        System.out.println("sono nel controller madonna impestata" + jsonParameters);
        test();
    }

    private void test(){
        System.out.println("sono nel controller madonna impestata" + jsonParameters);
    }
}
