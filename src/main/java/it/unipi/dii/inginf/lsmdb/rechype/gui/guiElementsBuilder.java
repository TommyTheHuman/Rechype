package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.geometry.Orientation;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class guiElementsBuilder {

    public HBox createUserBlock(User user){
        HBox block = new HBox();
        Text userNode = new Text(user.getUsername());
        Text ageNode = new Text(String.valueOf(user.getAge()));
        ImageView countryNode = null;
        ImageView levelNode = null;

        InputStream inputFlag;
        inputFlag = guiElementsBuilder.class.getResourceAsStream("/images/flags/" + user.getCountry() + ".png");
        InputStream inputAvatar = guiElementsBuilder.class.getResourceAsStream("/images/levels/" + String.valueOf(user.getLevel()) + ".png");

        if (inputFlag == null) {
            inputFlag = guiElementsBuilder.class.getResourceAsStream("/images/flags/Default.png");

        }
        countryNode = new ImageView(new Image(inputFlag));
        levelNode = new ImageView(new Image(inputAvatar,50, 50, false, false));


        block.getChildren().addAll(levelNode, userNode, ageNode, countryNode);

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);

        block.setId(user.getUsername());

        block.setOnMouseClicked((MouseEvent e) ->{
            System.out.println("o porco dio " + user.getUsername());

            JSONObject par = new JSONObject().append("id", user.getUsername());

            //System.out.println(par.get("id").toString());

            Main.changeScene("UserProfile", par);
        });
        return block;
    }
}
