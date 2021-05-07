package it.unipi.dii.inginf.lsmdb.rechype.user;

public class UserServiceFactory {

    private UserServiceFactory(){}

    public static UserServiceFactory create(){
        return new UserServiceFactory();
    }

    public UserService getService(){
        return new UserServiceImpl();
    }

}
