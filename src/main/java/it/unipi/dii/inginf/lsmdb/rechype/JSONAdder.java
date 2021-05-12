package it.unipi.dii.inginf.lsmdb.rechype;

import org.json.JSONObject;

public abstract class JSONAdder{

    public JSONObject jsonParameters;

    public void setGui(){
        return;
    }

    public void setParameters(JSONObject parameters){
        jsonParameters = parameters;
    }

    public JSONObject getParameters() {
        return jsonParameters;
    }


}
