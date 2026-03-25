package handler;

import io.javalin.http.Context;
import requestresult.ClearApplicationRequest;
import model.endpointresults.ClearApplicationResult;
import service.ClearApplicationService;

public class ClearApplicationHandler {

    private final ClearApplicationService clearAppService;
    private final JsonToRequestConverter<ClearApplicationRequest> deserializer;
    private final ResultToJsonStringConverter serializer;

    public ClearApplicationHandler(ClearApplicationService clearAppService) {
        this.clearAppService = clearAppService;
        deserializer = new JsonToRequestConverter<>(ClearApplicationRequest.class);
        serializer = ResultToJsonStringConverter.getInstance();
    }

    public void handleClearApplication(Context context) {
        ClearApplicationRequest req = deserializer.convert(context.body());
        // req object is kept to allow for future change in
        // required data but is empty and currently returns null
        if (req == null) {
            req = new ClearApplicationRequest();
        }
        ClearApplicationResult res = clearAppService.clear(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

}
