import it.unipi.dii.inginf.lsmdb.rechype.user.*;

public class Main {
    private static final UserServiceFactory factory=UserServiceFactory.create();

    public static void main(String[] args){
        UserService userService=factory.getService();
        if(userService.login("prova", "prova")){
            System.out.println("eureka!");
        }
    }
}
