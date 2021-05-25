package it.unipi.dii.inginf.lsmdb.rechype.profile;

import org.bson.Document;

import java.util.List;

public interface ProfileService {

    Profile getProfile(String user);

    String createProfile(String user);

    String deleteProfile(String user);

    String addMeal(String title, String type, List<Document> recipes, List<Document> drinks, String username);

    String deleteMeal(String title, String username);

    String addFridge(List<Document> ingredients, String username);

    String deleteIngredient(String username, String ingredient);
}
