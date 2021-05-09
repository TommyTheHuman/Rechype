package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.user.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class Main extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainScene = new Scene(loadFXML("HomePage"), 1000, 700);
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private static final UserServiceFactory factory=UserServiceFactory.create();

    public static void main(String[] args){

        launch(args);
        UserService userService=factory.getService();

    }

    private static Parent loadFXML(String fxml){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/" + fxml + ".fxml"));
            return fxmlLoader.load();
        }catch (IOException ie){
            System.out.println(fxml);
            LogManager.getLogger(Main.class.getName()).error("IO: Failed to load resources");
        }
        return null;
    }

    static void changeScene(String fxml){
        mainScene.setRoot(loadFXML(fxml));
    }
}
