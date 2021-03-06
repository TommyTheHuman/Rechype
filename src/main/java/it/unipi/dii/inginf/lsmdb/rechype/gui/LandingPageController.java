package it.unipi.dii.inginf.lsmdb.rechype.gui;


import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileService;
import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;

public class LandingPageController extends JSONAdder implements Initializable {


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

    private UserService userService;

    private ProfileService profileService;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userService = UserServiceFactory.create().getService();

        profileService = ProfileServiceFactory.create().getService();

        //prevent the user from insert letters into the text field
        regAge.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                regAge.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        loginBtn.setOnAction(event -> {
            String username = loginUsername.getText();
            String password = loginPassword.getText();

            if(username.equals("Admin") && password.equals("Admin")){
                Main.changeScene("AdminPage", new JSONObject());
            }

            if(userService.login(username, password)){
                Main.changeScene("HomePage", new JSONObject());
            }else{
                loginMsg.setText("Username or password \nare incorrect");
                loginMsg.setStyle("-fx-text-fill: red;");
            }

        });

        registerBtn.setOnAction(event -> {
            String result = "";
            String username = regUsername.getText();
            String password = regPassword.getText();
            String confPassword = regConfirmPassword.getText();
            String age = regAge.getText();
            String country;
            String resultProfile;
            int ageNum;

            clearFields();

            //All the fields must be filled
            if (username.equals("") || password.equals("") || confPassword.equals("") || age.equals("") || regCountry.getSelectionModel().isEmpty()) {
                regMsg.setText("All fields must be filled");
                regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
            }else {
                country = regCountry.getValue().toString();
                ageNum = Integer.parseInt(age);

                if (!password.equals(confPassword)) {
                    regMsg.setText("You must insert the same password in both fields");
                    regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                    return;
                } else {
                    //here all the fileds are full and the 2 password fields matches
                    result = userService.register(username, password, country, ageNum);
                }
            }

            if(result.equals("usernameProb")){
                regMsg.setText("Username already in use, try a different one");
                regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                return;
            }
            else if(result.equals("Abort")){
                regMsg.setText("Error occurred during the registration");
                regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                return;
            }
            else{
                //create profile and checking if the creation goes well
                resultProfile = profileService.createProfile(username);
                if(resultProfile.equals("ProfileOk")){
                    // here I have created all the necessary entities correctly
                    Main.changeScene("HomePage", new JSONObject());
                    return;
                }
                else{
                    //if the profile is not created the user must be deleted
                    result=userService.deleteUser(username);
                    if(result.equals("Abort")){
                        System.out.println("inconsistency between user and profile will be solved during parsing");
                    }else{
                        System.out.println("consistency between user and profile solved");
                    }
                    regMsg.setText("Error occurred during the registration");
                    regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                }
            }
        });

        //combobox population
        ObservableList<String> nations = getNations();
        regCountry.setItems(nations);

    }
    //function to get all the nation
    public static ObservableList<String> getNations(){
        String[] countries = Locale.getISOCountries();
        int maxSize = countries.length;

        List<String> nations = new ArrayList<>();

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
