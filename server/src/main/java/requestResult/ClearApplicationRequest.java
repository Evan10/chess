package requestResult;

public record ClearApplicationRequest()  implements NullCheckable{
    public boolean containsNullField(){
        return false;
    }
}
