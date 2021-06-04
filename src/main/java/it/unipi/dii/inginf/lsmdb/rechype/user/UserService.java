package it.unipi.dii.inginf.lsmdb.rechype.user;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

/**
 * A class that represents the services offered by the package user, it's the middleware connection between dao and gui
 *
 */

public interface UserService {

    boolean login(String user, String pass);
    String register(String username, String password, String country, int age);
    User getLoggedUser();
    void setLockSuggestions(boolean val);
    boolean getLockSuggestions();
    Document getUserById(String id);
    List<User> searchUser(String text, int offset, int quantity, JSONObject filters);
    JSONObject getCachedUser(String key);
    String addNewRecipe(Document doc, String type);
    String deleteUser(String username);
    List<Document> getRecipes(String username);
    String addFollow(String myName, String userName, String btnStatus);
    void setLoggedUser(User user); //delete
    Boolean checkForFollow(String myName, String userName);
    boolean checkRecipeLike(String _id, String type);
    boolean checkSavedRecipe(String _id, String type);
    String removeRecipe(String _id, String type);
    Document getRecipeAndDrinks(String user);
    String banUser(String user);
    List<Document> getSuggestedRecipes();
    List<Document> getSuggestedDrinks();
    List<Document> getSuggestedUsers();
    List<Document> getBestUsers();
    List<Document> getDrinks(String text);
    List<Document> getTophealthyUsers(String level);
    List<Document> getMostSavedRecipes(String category);
}




