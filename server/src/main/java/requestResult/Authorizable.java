package requestResult;

import model.AuthData;

public interface Authorizable<T extends Authorizable<T>> {
    public T withAuth(AuthData authData);
}
