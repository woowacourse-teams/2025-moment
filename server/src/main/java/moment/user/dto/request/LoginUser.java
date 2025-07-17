package moment.user.dto.request;

public record LoginUser(Long id) {

    public static LoginUser from(Long id) {
        return new LoginUser(id);
    }
}
