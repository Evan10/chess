package util;
import io.javalin.router.matcher.PathSegment;
import kotlin.Pair;

import java.util.UUID;
import java.util.regex.Pattern;

public class Util {

    public static String newUUID(){
        return UUID.randomUUID().toString();
    }



    public record PasswordValidationResult(String reason, boolean isValid) {
    }

    private static final Pattern INVALID_CHARS = Pattern.compile("[\\[\\](){}<>|?*\"',.\\-]");
    private static final Pattern NUMBER = Pattern.compile("\\d");
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern SYMBOL = Pattern.compile("[!@$~%#&^]");
    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    public static PasswordValidationResult isValidPassword(String password){

        if(password== null || password.isBlank()){
            return new PasswordValidationResult("Password cannot be blank", false);
        }

        boolean isValid = true;
        StringBuilder reason = new StringBuilder();

        if(INVALID_CHARS.matcher(password).find()){
            isValid = false;
            reason.append("Password cannot contain invalid character: [](){}<>|?*\"',.- \n");
        }
        if(!NUMBER.matcher(password).find()){
            isValid = false;
            reason.append("Password must contain number \n");
        }
        if(!UPPER.matcher(password).find()){
            isValid = false;
            reason.append("Password must contain uppercase letter \n");
        }
        if(!LOWER.matcher(password).find()){
            isValid = false;
            reason.append("Password must contain lowercase letter \n");
        }
        if(!SYMBOL.matcher(password).find()){
            isValid = false;
            reason.append("Password must contain symbol: !@$~%#&^ \n");
        }
        if(password.length()<MINIMUM_PASSWORD_LENGTH){
            isValid=false;
            reason.append("Password must contain at least "+ MINIMUM_PASSWORD_LENGTH + "characters \n");
        }

        return new PasswordValidationResult(reason.toString(), isValid);
    }



}
