package moment.notification.domain;

public class DeepLinkGenerator {

    public static String generate(NotificationType notificationType, SourceData sourceData) {
        return switch (notificationType) {
            case NEW_COMMENT_ON_MOMENT, MOMENT_LIKED ->
                "/groups/" + sourceData.getLong("groupId") + "/collection/my-moment";
            case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
                "/groups/" + sourceData.getLong("groupId") + "/today-moment";
            case GROUP_KICKED -> null;
            case COMMENT_LIKED ->
                "/groups/" + sourceData.getLong("groupId") + "/collection/my-comment";
        };
    }
}
