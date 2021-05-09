package it.unipi.dii.inginf.lsmdb.rechype;

import org.json.JSONObject;

public abstract class JSONAdder{

    public JSONObject jsonParameters;

    public void setParameters(JSONObject parameters) {
        System.out.println("sono nella classe abstract madonna cane" + parameters);
        this.jsonParameters = parameters;
    }

    public JSONObject getParameters() {
        return jsonParameters;
    }




}
