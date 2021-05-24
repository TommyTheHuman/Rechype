package it.unipi.dii.inginf.lsmdb.rechype.user;

import org.bson.Document;
import it.unipi.dii.inginf.lsmdb.rechype.recipe.Recipe;
import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

/**
 * A class that represents the services offered by the package user, it's the middleware connection between dao and gui
 *
 */

public interface UserService {

    boolean login(String user, String pass);
    String register(String username, String password, String confPassword, String country, int age);
    User getLoggedUser();
    List<User> searchUser(String text, int offset, int quantity);
    JSONObject getCachedUser(String key);
    String addNewRecipe(Document doc);
    String deleteUser(String username);
    List<Document> getUserRecipe(String username);
    List<Document> getNestedRecipes(String user);
    String addFollow(String myName, String userName, String btnStatus);
    Boolean checkForFollow(String myName, String userName);

}




