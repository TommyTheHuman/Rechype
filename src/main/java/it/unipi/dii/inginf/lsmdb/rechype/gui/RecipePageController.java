package it.unipi.dii.inginf.lsmdb.rechype.gui;

import it.unipi.dii.inginf.lsmdb.rechype.util.JSONAdder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

public class RecipePageController extends JSONAdder implements Initializable {

    @FXML private Text authorLabel;
    @FXML private Text Name;
    @FXML private Text DescriptionText;
    @FXML private Text Kcal;
    @FXML private Text WeightPerServing;
    @FXML private Text ReadyInMinutes;
    @FXML private Text Likes;
    @FXML private Text MethodText;
    @FXML private PieChart NutritionsPie;
    private ObservableList<PieChart.Data> pieData;


    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void setGui(){
        authorLabel.setText("Author: "+jsonParameters.getString("author"));
        Name.setText("title: "+jsonParameters.getString("name"));
        DescriptionText.setText(jsonParameters.getString("description"));
        MethodText.setText(jsonParameters.getString("method"));
        WeightPerServing.setText("Weight Per Serving: "+String.valueOf(jsonParameters.getInt("weightPerServing"))+" g");
        ReadyInMinutes.setText("Ready in Minutes: "+String.valueOf(jsonParameters.getInt("readyInMinutes")));
        Likes.setText("Likes: "+String.valueOf(jsonParameters.getInt("likes")));

        JSONArray nutrients=jsonParameters.getJSONArray("nutrients");
        if(nutrients.getJSONObject(0).get("amount") instanceof Integer) {
            int kcalAmount = nutrients.getJSONObject(0).getInt("amount");
            Kcal.setText("Kcal: " + String.valueOf(kcalAmount));
        }
        else {
            double kcalAmount = nutrients.getJSONObject(0).getDouble("amount");
            Kcal.setText("Kcal: " + String.valueOf(kcalAmount));
        }

        //Setting the Piechart
        pieData= FXCollections.observableArrayList();
        pieData.add(new PieChart.Data("Fats", nutrients.getJSONObject(1).get("amount") instanceof Integer?
        nutrients.getJSONObject(1).getInt("amount") : nutrients.getJSONObject(1).getDouble("amount")));

        pieData.add(new PieChart.Data("Carbohydrates", nutrients.getJSONObject(2).get("amount") instanceof Integer?
        nutrients.getJSONObject(2).getInt("amount") : nutrients.getJSONObject(2).getDouble("amount")));

        pieData.add(new PieChart.Data("Sugar", nutrients.getJSONObject(3).get("amount") instanceof Integer?
        nutrients.getJSONObject(3).getInt("amount") : nutrients.getJSONObject(3).getDouble("amount")));

        pieData.add(new PieChart.Data("Protein", nutrients.getJSONObject(4).get("amount") instanceof Integer?
        nutrients.getJSONObject(4).getInt("amount") : nutrients.getJSONObject(4).getDouble("amount")));

        pieData.add(new PieChart.Data("Fiber", nutrients.getJSONObject(5).get("amount") instanceof Integer?
        nutrients.getJSONObject(5).getInt("amount") : nutrients.getJSONObject(5).getDouble("amount")));

        pieData.add(new PieChart.Data("Calcium", nutrients.getJSONObject(6).get("amount") instanceof Integer?
        nutrients.getJSONObject(6).getInt("amount") : nutrients.getJSONObject(6).getDouble("amount")));

        NutritionsPie.setData(pieData);
        NutritionsPie.setLegendVisible(false);
        NutritionsPie.setTitle("Nutritional Information");

        //add the control for the mg case and other measures

    }
}
