package client;

public enum ClientState {

    LOGGED_IN("Logged in"),
    LOGGED_OUT("Logged out");
    public final String name;
    ClientState(String name){
        this.name = name;
    }
}
