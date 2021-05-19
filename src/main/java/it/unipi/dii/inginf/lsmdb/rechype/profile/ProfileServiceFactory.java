package it.unipi.dii.inginf.lsmdb.rechype.profile;



public class ProfileServiceFactory {
    public static ProfileServiceFactory create() {
        return new ProfileServiceFactory();
    }

    public ProfileService getService() {
        return new ProfileServiceImpl();
    }
}
