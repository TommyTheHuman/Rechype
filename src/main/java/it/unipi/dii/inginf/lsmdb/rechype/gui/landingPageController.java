package it.unipi.dii.inginf.lsmdb.rechype.gui;



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
import org.w3c.dom.Text;

import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;


/**
 * Initializable è necessario?
 */
public class landingPageController implements Initializable {

    @FXML
    public Button registerBtn;
    public TextField regUsername;
    public PasswordField regPassword;
    public PasswordField regConfirmPassword;
    public ComboBox regCountry;
    public TextField regAge;
    public Label regMsg;

    public Button loginBtn;
    public TextField loginUsername;
    public PasswordField loginPassword;

    UserServiceFactory userServiceFactory;
    UserService userService;



    



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
                String age = regAge.getText();
                String country;

                //funzione che svuota i campi
                clearFields();

                int ageNum;
                //controllo che tutti i campi siano rimepiti
                if(username.equals("") || password.equals("") || confPassword.equals("") || age.equals("") || regCountry.getSelectionModel().isEmpty()){
                    //errore di compilazione
                    regMsg.setText("All fields must be filled");
                    regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                }else {
                    //posso prendere la nazione e trasformare in numero l'età
                    country = regCountry.getValue().toString();
                    ageNum = Integer.parseInt(age);

                    //controllo se le password sono uguali
                    if (!password.equals(confPassword)) {
                        //errore perchè le password non sono uguali
                        regMsg.setText("You must insert the same password in both fields");
                        regMsg.setStyle("-fx-text-fill: red; -fx-background-color: transparent");
                    }else {
                        //a questo punto ho tutti i dati per riempire il DB con un trycatch per ora inserisco e basta
                        userService.register(username, password, confPassword, country, ageNum);
                    }
                }
                //userService.register(username, password, confPassword, "italy");

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
        ObservableList<String> listaNazioni = FXCollections.observableArrayList(nations);
        return listaNazioni;
    }

    private void clearFields(){
        regUsername.setText("");
        regPassword.setText("");
        regConfirmPassword.setText("");
        //regCountry.setValue(null);
        regCountry.valueProperty().set(null);
        regAge.setText("");
        regMsg.setText("");
    }
}
