package handler;

import io.javalin.http.Context;
import requestResult.ClearApplicationRequest;
import requestResult.ClearApplicationResult;
import service.ClearApplicationService;

public class ClearApplicationHandler {

    private final ClearApplicationService clearAppService;
    private final JsonToRequestConverter<ClearApplicationRequest> deserializer;
    private final ResultToJsonStringConverter serializer;

    public ClearApplicationHandler(ClearApplicationService clearAppService){
        this.clearAppService=clearAppService;
        deserializer = new JsonToRequestConverter<>(ClearApplicationRequest.class);
        serializer = ResultToJsonStringConverter.getInstance();
    }

    public void handleClearApplication(Context context){
        ClearApplicationRequest req = deserializer.convert(context.body());
        ClearApplicationResult res = clearAppService.clear(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

}
