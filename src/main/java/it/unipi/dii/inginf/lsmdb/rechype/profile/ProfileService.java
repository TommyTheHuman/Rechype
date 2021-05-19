package it.unipi.dii.inginf.lsmdb.rechype.profile;

public interface ProfileService {

    Profile getProfile(String user);

    String createProfile(String user);

}
