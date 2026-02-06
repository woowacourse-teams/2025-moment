package moment.notification.domain;

public class DeepLinkGenerator {

    public static String generate(NotificationType notificationType, SourceData sourceData) {
        Long groupId = sourceData.getLong("groupId");
        return switch (notificationType) {
            case NEW_COMMENT_ON_MOMENT, MOMENT_LIKED ->
                groupId != null ? "/groups/" + groupId + "/collection/my-moment" : null;
            case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
                "/groups/" + groupId + "/today-moment";
            case GROUP_KICKED -> null;
            case COMMENT_LIKED ->
                groupId != null ? "/groups/" + groupId + "/collection/my-comment" : null;
        };
    }
}
