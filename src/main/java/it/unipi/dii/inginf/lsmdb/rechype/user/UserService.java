package it.unipi.dii.inginf.lsmdb.rechype.user;

/**
 * A class that represents the services offered by the package user, it's the middleware connection between dao and gui
 *
 */

public interface UserService {

    boolean login(String user, String pass);
    String register(String username, String password, String confPassword, String country, int age);
    User getLoggedUser();

}




