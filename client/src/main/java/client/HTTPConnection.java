package client;

import java.net.http.HttpClient;
import java.util.Locale;

public class HTTPConnection{


    private static final HttpClient client = HttpClient.newBuilder().build();

    private String host;
    private int port;
    private String url;

    public HTTPConnection(String host, int port){
        this.host = host;
        this.port = port;
        url = String.format(Locale.getDefault(),"http://%s:%d",host,port);
    }





    public void close(){
        client.close();
    }

}
