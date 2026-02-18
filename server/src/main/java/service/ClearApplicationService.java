package service;

import dataaccess.DAO;
import request_result.ClearApplicationRequest;
import request_result.ClearApplicationResult;

import java.util.Collection;

public class ClearApplicationService {

    private Collection<DAO> dataAccessObjects;

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

}
