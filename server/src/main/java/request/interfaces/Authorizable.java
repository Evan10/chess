package request.interfaces;

import model.AuthData;

public interface Authorizable<T extends Authorizable<T>> {
    T withAuth(AuthData authData);
}
