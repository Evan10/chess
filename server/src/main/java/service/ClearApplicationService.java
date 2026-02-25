package service;

import dataAccess.DAO;
import requestResult.ClearApplicationRequest;
import requestResult.ClearApplicationResult;
import util.Constants;

import java.util.Collection;

public class ClearApplicationService {

    private final Collection<DAO> dataAccessObjects;

    public ClearApplicationService(Collection<DAO> dataAccessObjects){
        this.dataAccessObjects=dataAccessObjects;
    }

    public ClearApplicationResult clear(ClearApplicationRequest req){
        if(req == null){
            return new ClearApplicationResult(Constants.SERVER_ERROR,"Error: no request object provided");
        }
        boolean success = true;
        for(DAO dao : dataAccessObjects){
             if(!dao.clear())
                 success = false;
        }
        int responseCode = success? Constants.OK :Constants.SERVER_ERROR;
        String message = success?"":"Error: Unable to clear application data";
        return new ClearApplicationResult(responseCode,message);
    }

    //Not a handler endpoint; used for unit testing
    public boolean areDAOsEmpty(){
        for(DAO dao : dataAccessObjects){
            if(!dao.isEmpty()){
                return false;
            }
        }
        return true;
    }

}
