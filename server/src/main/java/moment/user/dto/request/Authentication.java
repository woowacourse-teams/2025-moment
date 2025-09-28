package moment.user.dto.request;

public record Authentication(Long id) {
    public static Authentication from(Long id) {
        return new Authentication(id);
    }
}
