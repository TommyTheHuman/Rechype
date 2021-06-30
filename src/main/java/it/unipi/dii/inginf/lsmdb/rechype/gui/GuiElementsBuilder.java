package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.drink.Drink;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkService;
import it.unipi.dii.inginf.lsmdb.rechype.drink.DrinkServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.persistence.HaloDBDriver;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeService;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.RecipeServiceFactory;
import it.unipi.dii.inginf.lsmdb.rechype.user.User;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GuiElementsBuilder {

    List<String> idSelectedIngredient;

    public GuiElementsBuilder(){
        idSelectedIngredient = new ArrayList<>();
    }

    public HBox createUserBlock(User user){
        HBox block = new HBox();
        Text userNode = new Text(user.getUsername());
        Text ageNode = new Text(String.valueOf(user.getAge()));
        ImageView countryNode;
        ImageView levelNode;
        InputStream inputFlag;
        inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/" + user.getCountry() + ".png");

        //setting the icon based on level
        InputStream inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        if(user.getLevel()<=5){
            inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/0.png");
        }
        else if(user.getLevel()<=10 && user.getLevel()>5){
            inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/1.png");
        }
        else if(user.getLevel()>10){
            inputAvatar = GuiElementsBuilder.class.getResourceAsStream("/images/levels/2.png");
        }

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
            JSONObject par = new JSONObject().put("_id", user.getUsername());
            Main.changeScene("UserProfile", par);
        });
        return block;
    }

    public HBox createSimpleBlock(Text text, String id){
        HBox block = new HBox();

        block.getChildren().addAll(text);
        block.setStyle("-fx-padding: 10px; -fx-cursor: hand;");

        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);

        block.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", id);
            Main.changeScene("UserProfile", par);
        });
        return block;
    }

    public HBox createSimpleUserBlock(User user){
       HBox block=createSimpleBlock(new Text(user.getUsername()), user.getUsername());
       ImageView countryNode;
       InputStream inputFlag;
       inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/" + user.getCountry() + ".png");
       if (inputFlag == null) {
            inputFlag = GuiElementsBuilder.class.getResourceAsStream("/images/flags/Default.png");
       }
       countryNode = new ImageView(new Image(inputFlag));
       block.getChildren().add(countryNode);

       block.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", user.getUsername());
            Main.changeScene("UserProfile", par);
       });
       return block;
    }

    public HBox createSimpleRecipeBlock(Recipe recipe){
        HBox block=createSimpleBlock(new Text(recipe.getName()), recipe.getId());
        block.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", recipe.getId());
            Main.changeScene("RecipePage", par);
        });
        return block;
    }

    public HBox createSimpleDrinkBlock(Drink drink){
        HBox block = createSimpleBlock(new Text(drink.getName()), drink.getId());
        block.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", drink.getId());
            Main.changeScene("DrinkPage", par);
        });
        return block;
    }

    //creating an ingredient block for a selected search vbox
    public HBox createIngredientBlock(Ingredient ingredient, VBox selectedIngredientVBox){

        HBox block = new HBox();
        Text nameNode = new Text(ingredient.getName());
        Label amount = new Label("");
        IngredientService ingredientService = IngredientServiceFactory.create().getService();

        //setting image from k-v
        byte[] imgBytes=ingredientService.getCachedImage(ingredient.getName());
        InputStream imageStream;
        if(imgBytes==null){
            imageStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/no.png");
        }else{
            imageStream = new ByteArrayInputStream(imgBytes);
        }

        ImageView imageNode =
        new ImageView(new Image(imageStream, 50, 50, false, false));

        block.getChildren().addAll(imageNode, nameNode, amount);
        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        block.setId(ingredient.getName());

        block.setOnMouseClicked((MouseEvent e) ->{
            if(!idSelectedIngredient.contains(ingredient.getName())){
                idSelectedIngredient.add(ingredient.getName());
                Text selectedNode = new Text(ingredient.getName());
                Button deleteIngredient = new Button("Delete");
                TextField quantity = new TextField();
                quantity.setPrefWidth(50);
                Text grams = new Text("g");
                HBox blockSelected = new HBox(selectedNode);
                HBox controllerIngredient;
                if(selectedIngredientVBox.getId().equals("NoGrams"))
                    controllerIngredient = new HBox(deleteIngredient, quantity);
                else
                    controllerIngredient = new HBox(deleteIngredient, quantity, grams);
                controllerIngredient.setSpacing(10.0);
                VBox vbox = new VBox();
                vbox.getChildren().addAll(blockSelected, controllerIngredient, new Separator(Orientation.HORIZONTAL));
                vbox.setSpacing(10.0);
                blockSelected.setAlignment(Pos.CENTER_LEFT);
                blockSelected.setSpacing(10.0);
                selectedIngredientVBox.getChildren().addAll(vbox);

                deleteIngredient.setOnAction(event -> {
                    selectedIngredientVBox.getChildren().remove(vbox);
                    idSelectedIngredient.remove(ingredient.getName());
                });
            }
        });
        block.setStyle("-fx-background-color: white");
        return block;

    }

    public HBox createSimpleIngredientBlock(JSONObject ingredient){
        HBox block = new HBox();
        Text nameNode;
        IngredientService ingredientService = IngredientServiceFactory.create().getService();
        nameNode = new Text(ingredient.getString("ingredient"));
        Text amount=null;
        if(ingredient.has("amount")) {
            if (ingredient.get("amount") instanceof String)
                amount = new Text("Amount: " + ingredient.getString("amount"));
            else
                amount = new Text("Amount: " + ingredient.get("amount"));
        }

        //setting image from k-v
        byte[] imgBytes;
        if(ingredient.has("_id"))
            imgBytes=ingredientService.getCachedImage(ingredient.getString("_id"));
        else
            imgBytes=ingredientService.getCachedImage(ingredient.getString("ingredient"));
        InputStream imageStream;
        if(imgBytes==null){
            imageStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/no.png");
        }else{
            imageStream = new ByteArrayInputStream(imgBytes);
        }
        ImageView imageNode =
        new ImageView(new Image(imageStream, 50, 50, false, false));

        if(amount!=null)
            block.getChildren().addAll(imageNode, new VBox(nameNode, amount));
        else
            block.getChildren().addAll(imageNode, new VBox(nameNode));
        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        block.setStyle("-fx-background-color: white");
        return block;
    }

    public HBox createRecipeBlock(Recipe recipe) {
        HBox mainContainer=new HBox();
        VBox textContainer=new VBox();
        HBox iconContainer=new HBox();
        ImageView imageRecipe=null;
        InputStream inputStream;
        RecipeService recipeService = RecipeServiceFactory.create().getService();

        //setting all the icons
        if(recipe.isVegan()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegan_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegan_e.png");
        }
        ImageView veganIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        veganIcon.setCache(true);

        if(recipe.isVegetarian()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegetarian_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/vegetarian_e.png");
        }
        ImageView vegetarianIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        vegetarianIcon.setCache(true);

        if(recipe.isGlutenFree()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/gluten_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/gluten_e.png");
        }
        ImageView glutenIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        glutenIcon.setCache(true);

        if(recipe.isDairyFree()){
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/dairy_on_e.png");
        }else{
            inputStream=GuiElementsBuilder.class.getResourceAsStream("/images/icons/dairy_e.png");
        }
        ImageView dairyIcon=new ImageView(new Image(inputStream, 20, 20, false, false));
        dairyIcon.setCache(true);

        //setting image from k-v
        byte[] imgBytes=recipeService.getCachedImage(recipe.getId());
        InputStream imageStream;
        if(imgBytes==null){
            imageStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        }else{
            imageStream = new ByteArrayInputStream(imgBytes);
        }
        imageRecipe = new ImageView(new Image(imageStream, 50, 50, false, false));

        //setting the price icon
        Text priceIcon = new Text(Recipe.getPriceSymbol(recipe.getPricePerServing()));
        priceIcon.setStyle("-fx-font-size: 16px");
        iconContainer.getChildren().addAll(veganIcon, vegetarianIcon, dairyIcon, glutenIcon, priceIcon);
        iconContainer.setSpacing(10);
        String name=recipe.getName();
        if(name.length()>40){
            name=name.substring(0, 40)+"...";
        }
        Text text=new Text(name);
        textContainer.getChildren().addAll(iconContainer, text);

        mainContainer.getChildren().addAll(imageRecipe, textContainer);
        mainContainer.setSpacing(5);
        mainContainer.setId("mainContainer");

        mainContainer.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", recipe.getId());
            Main.changeScene("RecipePage", par);
        });
        mainContainer.setStyle("-fx-background-color: white");
        return mainContainer;
    }

    public HBox createDrinkBlock(Drink drink){
        DrinkService drinkService = DrinkServiceFactory.create().getService();
        HBox block=new HBox();
        VBox textBlock=new VBox();
        block.setAlignment(Pos.CENTER_LEFT);
        block.setSpacing(10.0);
        textBlock.getChildren().addAll(new Text("name: "+drink.getName()),new Text("author: "+drink.getAuthor()), new Text("tag: "+drink.getTag()));
        //setting image from k-v
        byte[] imgBytes=drinkService.getCachedImage(drink.getId());
        InputStream imageStream;
        if(imgBytes==null){
            imageStream = GuiElementsBuilder.class.getResourceAsStream("/images/icons/cloche.png");
        }else{
            imageStream = new ByteArrayInputStream(imgBytes);
        }
        ImageView drinkImage = new ImageView(new Image(imageStream, 50, 50, false, false));
        block.getChildren().addAll(drinkImage, textBlock);

        block.setOnMouseClicked((MouseEvent e) ->{
            JSONObject par = new JSONObject().put("_id", drink.getId());
            Main.changeScene("DrinkPage", par);
        });
        block.setStyle("-fx-background-color: white");
        return block;
    }
}
