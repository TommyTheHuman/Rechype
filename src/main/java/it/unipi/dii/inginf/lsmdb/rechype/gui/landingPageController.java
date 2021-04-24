package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
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

    public Button registerBtn;
    public TextField regUsername;
    public PasswordField regPassword;
    public PasswordField regConfirmPassword;
    public ComboBox regCountry;

    public Button loginBtn;
    public TextField loginUsername;
    public PasswordField loginPassword;

    UserServiceFactory userServiceFactory;
    UserService userService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();

        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = loginUsername.getText();
                String password = loginPassword.getText();
                if(userService.login(username, password)){
                    regUsername.setText("LOGGATO");
                }else{
                    regUsername.setText("NON LOGGATO");
                }

            }
        });

        registerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = regUsername.getText();
                /* Gestire  password field */
                String password = regPassword.getText();
                String confPassword = regConfirmPassword.getText();
                //String country = regCountry.toString();

                userService.register(username, password, confPassword, "italy");
            }
        });

    }


}
