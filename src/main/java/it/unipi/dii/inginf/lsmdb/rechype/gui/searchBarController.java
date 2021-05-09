package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserService;
import it.unipi.dii.inginf.lsmdb.rechype.user.UserServiceFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class searchBarController extends JSONAdder implements Initializable {

    @FXML private Button searchBtn;
    @FXML private TextField searchText;
    @FXML private VBox resultBox;
    @FXML private Button closeSearch;

    @FXML private AnchorPane searchAnchor;

    private guiElementsBuilder builder;

    private UserServiceFactory userServiceFactory;
    private UserService userService;
    private User loggedUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        builder = new guiElementsBuilder();

        userServiceFactory = UserServiceFactory.create();
        userService = userServiceFactory.getService();
        loggedUser = userService.getLoggedUser();

        searchAnchor.setVisible(false);

        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = searchText.getText();
                List<User> listOfUsers = userService.searchUser(text);

                for(User user : listOfUsers){
                    resultBox.getChildren().addAll(builder.createUserBlock(user), new Separator(Orientation.HORIZONTAL));
                }

                resultBox.setStyle("-fx-background-color: white !important");
                searchAnchor.setVisible(true);
            }
        });

        closeSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                resultBox.getChildren().clear();
                searchAnchor.setVisible(false);
            }
        });

    }
}
