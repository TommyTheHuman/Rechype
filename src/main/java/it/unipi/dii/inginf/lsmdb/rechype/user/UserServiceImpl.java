package it.unipi.dii.inginf.lsmdb.rechype.user;

import org.bson.Document;
import org.json.JSONObject;

import java.util.List;

class UserServiceImpl implements UserService {
    private static UserDao userDao=new UserDao();
    private static User loggedUser;
    private static boolean lockSuggestions;

    public boolean login(String user, String pass){
        loggedUser = userDao.checkLogin(user, pass);
        if(loggedUser == null){
            return false;
        }
        return true;
    }

    //it is used to set the reload or not of the suggestions
    public void setLockSuggestions(boolean val){
        lockSuggestions=val;
    }

    public boolean getLockSuggestions(){
        return lockSuggestions;
    }

    public String register(String username, String password, String country, int age){
        JSONObject Json =  userDao.checkRegistration(username, password, country, age);
        String response = Json.get("response").toString();
        if(response.equals("RegOk")) {
            loggedUser = new User(Json.get("_id").toString(), Json.get("country").toString(),
                    Integer.parseInt(Json.get("age").toString()), Integer.parseInt(Json.get("level").toString()));
        }
        return response;
    }

    public User getLoggedUser(){
        setLockSuggestions(false);
        return loggedUser;
    }

    public List<User> searchUser(String text, int offset, int quantity, JSONObject filters) {
        return userDao.getUsersByText(text, offset, quantity, filters);
    }


    public String deleteUser(String username){
        if(userDao.deleteUser(username))
            return "DeleteOk";
        else
            return "Abort";
    }

    public Document getUserById(String id){ return userDao.getUserById(id); }
    public String addNewRecipe(Document doc, String type){ return userDao.addNestedRecipe(doc, loggedUser, type); }
    public boolean checkRecipeLike(String _id, String type){ return userDao.checkRecipeLike(getLoggedUser().getUsername(), _id, type); }
    public boolean checkSavedRecipe(String _id, String type){ return userDao.checkSavedRecipe(getLoggedUser().getUsername(), _id, type); }
    public String removeRecipe(String _id, String type){ return userDao.removeNestedRecipe(getLoggedUser().getUsername(), _id, type); }
    public String addFollow(String myName, String userName, String btnStatus) { return userDao.followUser(myName, userName, btnStatus);}
    public Boolean checkForFollow(String myName, String userName) {return userDao.checkUserFollow(myName, userName);}
    public List<Document> getRecipes(String username){ return userDao.getUserRecipe(username); }
    public Document getRecipeAndDrinks(String username){ return userDao.getUserRecipeAndDrinks(username); }
    public List<Document> getDrinks(String username) {return userDao.getDrinkRecipe(username);}
    public String banUser(String user){ return userDao.banUser(user); }
    public List<Document> getSuggestedRecipes() { return userDao.getSuggestedRecipes(getLoggedUser().getUsername()); }
    public List<Document> getSuggestedDrinks() { return userDao.getSuggestedDrinks(getLoggedUser().getUsername()); }
    public List<Document> getSuggestedUsers() { return userDao.getSuggestedUsers(getLoggedUser().getUsername()); }
    public List<Document> getBestUsers() { return userDao.getBestUsers(); }
    public List<Document> getMostSavedRecipes(String category) {return userDao.mostSavedRecipes(category);}
    public Boolean changeCountry(String country, String name) { return userDao.changeCountryToUser(country, name);}

}
