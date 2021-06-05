package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.Ingredient;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientService;
import it.unipi.dii.inginf.lsmdb.rechype.ingredient.IngredientServiceFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddIngredientController extends JSONAdder implements Initializable {

    @FXML private TextField ingredientText;
    @FXML private VBox searchedIngredientVBox;
    @FXML private VBox selectedIngredientVBox;
    @FXML private Button backToRecipeBtn;
    @FXML private ScrollPane scrollBoxIngredients;

    @FXML private Text inputGramsError;

    private IngredientService ingredientService;

    private GuiElementsBuilder builder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputGramsError.setOpacity(0);
        ingredientService = IngredientServiceFactory.create().getService();

        builder = new GuiElementsBuilder();

        backToRecipeBtn.setOnAction(event -> {
            JSONObject par = jsonParameters;
            StringBuilder finalIngredients = new StringBuilder();
            int counter = 0;

            // Retrieve the list of all the selected ingredients
            for(Node node: selectedIngredientVBox.getChildren()){
                if(node instanceof VBox){
                    HBox hbox = (HBox) ((VBox)node).getChildren().get(1);
                    TextField quantityFields = (TextField) hbox.getChildren().get(1);
                    String quantityString = quantityFields.getText();

                    if(quantityString.length() == 0){
                        inputGramsError.setOpacity(100);
                        return;
                    }
                    // Check if this node is the last, in this case don't append the comma.
                    if(counter == builder.idSelectedIngredient.size()-1){
                        if(!par.has("Drink")) {
                            finalIngredients.append(builder.idSelectedIngredient.get(counter)).append(": ").append(quantityString).append("g ");
                        }else{
                            finalIngredients.append(builder.idSelectedIngredient.get(counter)).append(": ").append(quantityString);
                        }
                    }else {
                        if(!par.has("Drink")){
                            finalIngredients.append(builder.idSelectedIngredient.get(counter)).append(": ").append(quantityString).append("g, ");
                        }else{
                            finalIngredients.append(builder.idSelectedIngredient.get(counter)).append(": ").append(quantityString).append(", ");
                        }
                    }
                    counter++;
                }
            }

            inputGramsError.setOpacity(0);
            // Update the json to pass to add recipe page with ingredients' string.
            par.remove("ingredients");
            par.put("ingredients", finalIngredients.toString());

            if(par.has("Drink")) {
                Main.changeScene("DrinkAdd", par);
            }
            else
                Main.changeScene("RecipeAdd", par);
        });

        // When the user types a more than 3 letters create the boxes containing the ingredient and display those in searchedIngredientVBox
        ingredientText.setOnKeyTyped(event -> {
            String text = ingredientText.getText();
            searchedIngredientVBox.getChildren().clear();
            if(text.length() > 2){
                for(Ingredient ingr: ingredientService.searchIngredients(text, 0, 10)){
                    searchedIngredientVBox.getChildren().addAll(builder.createIngredientBlock(ingr, selectedIngredientVBox), new Separator(Orientation.HORIZONTAL));
                }
            }
        });


        // Infinite scroll
        scrollBoxIngredients.vvalueProperty().addListener(new ChangeListener<>() {
            int offset = 0;

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (scrollBoxIngredients.getVvalue() == scrollBoxIngredients.getVmax()) {
                    offset = searchedIngredientVBox.getChildren().size() / 2;

                    List<Ingredient> listOfIngredients = ingredientService.searchIngredients(ingredientText.getText(), offset, 10);
                    for (Ingredient ingr : listOfIngredients) {
                        searchedIngredientVBox.getChildren().addAll(builder.createIngredientBlock(ingr, selectedIngredientVBox), new Separator(Orientation.HORIZONTAL));
                    }
                }
            }
        });
    }

    @Override
    public void setGui(){
        JSONObject par = jsonParameters;
        if(jsonParameters.has("Drink"))
            selectedIngredientVBox.setId("NoGrams");
        //if I enter this page and I already selected some ingredients
        if(!(par.get("ingredients").equals(""))){
            String ingredientsString = par.get("ingredients").toString();
            String[] singleIngredient = ingredientsString.trim().split(", ");
            for(String ingr: singleIngredient){
                if(!par.has("Drink")) {
                    ingr = ingr.substring(0, ingr.length() - 1);
                }
                String[] details = ingr.trim().split(":");
                builder.idSelectedIngredient.add(details[0]);

                details[1] = details[1].replaceAll("\\s+","");
                Text nameNode = new Text(details[0]);
                Text gramsNode = new Text("g");
                TextField quantity = new TextField(details[1]);
                quantity.setPrefWidth(50);
                Button deleteIngredient = new Button("Delete");
                HBox nameBox = new HBox(nameNode);
                HBox handleBox;
                if(par.has("Drink")) {
                    handleBox = new HBox(deleteIngredient, quantity);
                }
                else {
                    handleBox = new HBox(deleteIngredient, quantity, gramsNode);
                }
                handleBox.setSpacing(10.0);
                VBox vbox = new VBox(nameBox, handleBox, new Separator(Orientation.HORIZONTAL));
                vbox.setSpacing(10.0);
                selectedIngredientVBox.getChildren().addAll(vbox);

                deleteIngredient.setOnAction(event -> {
                    selectedIngredientVBox.getChildren().remove(vbox);
                    builder.idSelectedIngredient.remove(details[0]);
                });

            }
            par.remove("ingredients");
        }


    }
}
