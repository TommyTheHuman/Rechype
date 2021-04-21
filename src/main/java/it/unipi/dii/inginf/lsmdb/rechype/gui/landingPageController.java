package it.unipi.dii.inginf.lsmdb.rechype.gui;

import com.gluonhq.charm.glisten.control.DropdownButton;
import com.gluonhq.charm.glisten.control.TextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * Initializable è necessario?
 */
public class landingPageController implements Initializable {

    public Button loginBtn;
    public Button registerBtn;
    public TextField regUsername;
    public PasswordField regPassword;
    public PasswordField regConfirmPassword;
    public DropdownButton regCountry;
    public TextField loginUsername;
    public PasswordField loginPassword;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                regUsername.setText("fe");
            }
        });

    }


}
