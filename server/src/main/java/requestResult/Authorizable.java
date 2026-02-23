package requestResult;

public interface Authorizable<T extends Authorizable<T>> {
    public T withAuth(String authToken);
}
