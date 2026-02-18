package handler;

import requestResult.ClearApplicationRequest;
import requestResult.ClearApplicationResult;
import service.ClearApplicationService;

public class ClearDatabaseHandler {

    private final ClearApplicationService clearAppService;
    private final JsonToRequestConverter<ClearApplicationRequest> deserializer;
    private final ResultToJsonStringConverter serializer;

    public ClearDatabaseHandler(ClearApplicationService clearAppService){
        this.clearAppService=clearAppService;
        deserializer = new JsonToRequestConverter<>(ClearApplicationRequest.class);
        serializer = ResultToJsonStringConverter.getInstance();
    }

    public String handleClearApplication(String json){
        ClearApplicationRequest req = deserializer.convert(json);
        ClearApplicationResult res = clearAppService.clear(req);
        return serializer.resToString(res);
    }

}
