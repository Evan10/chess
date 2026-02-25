package service;

import dataAccess.DAO;
import requestResult.ClearApplicationRequest;
import requestResult.ClearApplicationResult;

import java.util.Collection;

public class ClearApplicationService {

    private final Collection<DAO> dataAccessObjects;

    public ClearApplicationService(Collection<DAO> dataAccessObjects){
        this.dataAccessObjects=dataAccessObjects;
    }

    public ClearApplicationResult clear(ClearApplicationRequest req){
        boolean success = true;
        for(DAO dao : dataAccessObjects){
             if(!dao.clear())
                 success = false;
        }
        int responseCode = success?200:500;
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
