package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class UserProfileController extends JSONAdder implements Initializable {

    @FXML private Text userText;
    private UserServiceFactory userServiceFactory;
    private UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
    }

    @Override
    public void setGui(){
        JSONObject fields=userService.getCachedUser(jsonParameters.getString("_id"));
        userText.setText(fields.getString("_id").toString());
    }
}
