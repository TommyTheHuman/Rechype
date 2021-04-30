package it.unipi.dii.inginf.lsmdb.rechype;

import it.unipi.dii.inginf.lsmdb.rechype.user.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainScene = new Scene(loadFXML("landing"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private static final UserServiceFactory factory=UserServiceFactory.create();

    public static void main(String[] args){

        launch(args);
        UserService userService=factory.getService();

    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void changeScene(String fxml) throws Exception{
        mainScene.setRoot(loadFXML(fxml));
    }
}
