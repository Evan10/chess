package model;

public record AuthData(String authToken, String username) {
    public boolean isValid(){
        return authToken != null && !authToken.isBlank() && username != null && !username.isBlank();
    }
}
