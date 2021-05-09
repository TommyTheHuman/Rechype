package it.unipi.dii.inginf.lsmdb.rechype.gui;



import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;


/**
 * Initializable è necessario?
 */
public class landingPageController extends JSONAdder implements Initializable {


    @FXML private Button registerBtn;
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;
    @FXML private PasswordField regConfirmPassword;
    @FXML private ComboBox regCountry;
    @FXML private TextField regAge;
    @FXML private Label regMsg;

    @FXML private Button loginBtn;
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMsg;

    private UserServiceFactory userServiceFactory;
    private UserService userService;



    



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();


        regAge.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    regAge.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });



        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = loginUsername.getText();
                String password = loginPassword.getText();
                if(userService.login(username, password)){
                    Main.changeScene("HomePage", new JSONObject());
                }else{
                    loginMsg.setText("Username or password \nare incorrect");
                    loginMsg.setStyle("-fx-text-fill: red;");
                }

            }
        });

        registerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String result = new String();
                String username = regUsername.getText();
                String password = regPassword.getText();
                String confPassword = regConfirmPassword.getText();
                String age = regAge.getText();
                String country;
                int ageNum;

                clearFields();

                if(username.equals("") || password.equals("") || confPassword.equals("") || age.equals("") || regCountry.getSelectionModel().isEmpty()){
                    regMsg.setText("All fields must be filled");
                    regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                }else {
                    country = regCountry.getValue().toString();
                    ageNum = Integer.parseInt(age);

                    if (!password.equals(confPassword)) {
                        regMsg.setText("You must insert the same password in both fields");
                        regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                    }else {
                        result = userService.register(username, password, confPassword, country, ageNum);
                    }
                }

                if(result.equals("RegOk")){
                    Main.changeScene("HomePage", new JSONObject());
                }else if(result.equals("Abort")){
                    regMsg.setText("Error occurred during the registration");
                    regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                }else if(result.equals("usernameProb")){
                    regMsg.setText("Username already in use, try a different one");
                    regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                }
            }
        });

        //popolazione del ComboBox
        ObservableList<String> nations = getNations();
        regCountry.setItems(nations);

    }





    private ObservableList<String> getNations(){
        String[] countries = Locale.getISOCountries();
        int maxSize = countries.length;

        List<String> nations = new ArrayList<String>();

        for(int i = 0; i < maxSize;i++){
            String country = countries[i];
            Locale locale = new Locale("en", country);
            String countryName = locale.getDisplayCountry(Locale.forLanguageTag("en_US"));
            nations.add(countryName);
        }

        Collections.sort(nations);
        ObservableList<String> nationsList = FXCollections.observableArrayList(nations);
        return nationsList;
    }

    private void clearFields(){
        regUsername.setText("");
        regPassword.setText("");
        regConfirmPassword.setText("");
        regAge.setText("");
        regMsg.setText("");
    }
}
