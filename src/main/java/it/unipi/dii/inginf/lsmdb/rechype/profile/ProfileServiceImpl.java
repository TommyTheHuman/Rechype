package it.unipi.dii.inginf.lsmdb.rechype.profile;

import it.unipi.dii.inginf.lsmdb.rechype.profile.ProfileDAO;
import org.bson.Document;

import java.util.List;

class ProfileServiceImpl implements ProfileService {
    private static ProfileDAO profileDAO=new ProfileDAO();

    public Profile getProfile(String user){
        return profileDAO.getProfileByUsername(user);
    }

    public String createProfile(String username){
        if(profileDAO.insertProfile(username)){
            return "ProfileOk";
        }else{
            return "Abort";
        }
    }

    public String deleteProfile(String username){
        if(profileDAO.deleteProfile(username)){
            return "DeleteOK";
        }else{
            return "Abort";
        }
    }

    public String addMeal(String title, String type, List<Document> recipes, List<Document> drinks, String username){
        if(profileDAO.addMealToProfile(title, type, recipes, drinks, username)){
            return "AddOK";
        }else{
            return "Abort";
        }
    }

    public String deleteMeal(String title, String username){
        if(profileDAO.deleteMealFromProfile(title, username)){
            return "DeleteMealOK";
        }else{
            return "Abort";
        }
    }

    @Override
    public String addFridge(List<Document> ingredients, String username) {
        if(profileDAO.addIngredientToFridge(ingredients, username)){
            return "AddOK";
        }else{
            return "Abort";
        }
    }

    @Override
    public String deleteIngredient(String username, String ingredient) {
        if(profileDAO.deleteIngredientFromProfile(username, ingredient)){
            return "DeleteIngrOK";
        }else{
            return "Abort";
        }
    }
}
