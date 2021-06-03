package it.unipi.dii.inginf.lsmdb.rechype.persistence;

import org.apache.logging.log4j.LogManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Load the configuration of the databases from resources/configDB.properties
 */

class DBConfigurations {
    public long timerNeo4j;
    public String defaultDBNeo4j;
    public String defaultDBMongo;
    public String Neo4jUri;
    public String MongoUri;
    public String Neo4jUser;
    public String Neo4jPassword;
    private static final DBConfigurations obj=new DBConfigurations();

    private DBConfigurations(){
        InputStream inputStream;
        Properties prop = new Properties();
        try{
            inputStream = getClass().getClassLoader().getResourceAsStream("configDB.properties");
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property 'file configDB.properties' not found in the classpath");
            }
        }catch(IOException io){
            LogManager.getLogger(DBConfigurations.class.getName()).fatal("Database: configuration not loaded");
            System.exit(-1);
        }
        timerNeo4j=Long.parseLong(prop.getProperty("timerNeo4j"));
        defaultDBNeo4j=prop.getProperty("defaultDBNeo4j");
        defaultDBMongo=prop.getProperty("defaultDBMongo");
        Neo4jUri=prop.getProperty("Neo4jUri");
        MongoUri=prop.getProperty("MongoUri");
        Neo4jUser=prop.getProperty("Neo4jUser");
        Neo4jPassword= prop.getProperty("Neo4jPassword");
    }

    public static DBConfigurations getObject(){ return obj; }
}
