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
    String register(String username, String password, String confPassword, String country, int age);
    User getLoggedUser();
    List<User> searchUser(String text, int offset, int quantity);
    JSONObject getCachedUser(String key);
    String addNewRecipe(Document doc, String type);
    String deleteUser(String username);
    boolean checkRecipeLike(String _id, String type);
    boolean checkSavedRecipe(String _id, String type);
    String removeRecipe(String _id, String type);
}




