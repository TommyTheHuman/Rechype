package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.user.*;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

import java.io.IOException;

public class Main extends Application {
    private static Main istance;
    private static Scene mainScene;
    private static final UserServiceFactory factory=UserServiceFactory.create();

    @Override
    public void start(Stage primaryStage) throws Exception{
        mainScene = new Scene(loadFXML("Landing", new JSONObject()), 1000, 700);
        istance=this;
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public static void main(String[] args){

        launch(args);
        UserService userService=factory.getService();

    }

    private static Parent loadFXML(String fxml, JSONObject par){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/" + fxml + ".fxml"));
            Parent returnValue =  fxmlLoader.load();
            JSONAdder controller = fxmlLoader.getController();
            controller.setGui(par);
            return returnValue;
        }catch (IOException ie){
            LogManager.getLogger(Main.class.getName()).error("IO: Failed to load resources");
        }
        return null;
    }

    public static void changeScene(String fxml, JSONObject parameters){
        mainScene.setRoot(loadFXML(fxml, parameters));
    }

    public static Main getInstance(){
        return istance;
    }

}
