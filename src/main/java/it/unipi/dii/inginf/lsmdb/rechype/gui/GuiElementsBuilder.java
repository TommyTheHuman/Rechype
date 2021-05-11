package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class GuiElementsBuilder {

    public HBox createUserBlock(User user){
        HBox block = new HBox();
        Text userNode = new Text(user.getUsername());
        Text ageNode = new Text(String.valueOf(user.getAge()));
        ImageView countryNode = null;
        ImageView levelNode = null;

        InputStream inputFlag;
        inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/" + user.getCountry() + ".png");
        InputStream inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/" + String.valueOf(user.getLevel()) + ".png");

        if (inputFlag == null) {
            inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/Default.png");
        }
        countryNode = new ImageView(new Image(inputFlag));
        levelNode = new ImageView(new Image(inputAvatar,50, 50, false, false));


        block.getChildren().addAll(levelNode, userNode, ageNode, countryNode);

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);

        block.setId(user.getUsername());

        block.setOnMouseClicked((MouseEvent e) ->{

            JSONObject par = new JSONObject().put("id", user.getUsername());

            Main.changeScene("UserProfile", par);
        });
        return block;
    }

    public HBox createIngredientBlock(Ingredient ingredient, VBox selectedIngredientVBox)throws IOException {

        HBox block = new HBox();
        Text nameNode = new Text(ingredient.getName());
        String imageName = ingredient.getImageUrl();
        String imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/" + imageName;
        ImageView imageNode = null;

        InputStream imageStream = new URL(imageUrl).openStream();
        imageNode = new ImageView(new Image(imageStream));
        block.getChildren().addAll(imageNode, nameNode);

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        block.setId(ingredient.getName());

        ImageView finalImageNode = imageNode;
        block.setOnMouseClicked((MouseEvent e) ->{
            Text selectedNode = new Text("Selected: " + ingredient.getName());
            HBox blockSelected = new HBox();
            blockSelected.getChildren().addAll(finalImageNode, nameNode);
            blockSelected.setAlignment(Pos.CENTER_LEFT);
            blockSelected.setSpacing(10.0);
            blockSelected.setId(ingredient.getName());
            selectedIngredientVBox.getChildren().addAll(blockSelected, new Separator(Orientation.HORIZONTAL));

        });
        return block;

    }
}
