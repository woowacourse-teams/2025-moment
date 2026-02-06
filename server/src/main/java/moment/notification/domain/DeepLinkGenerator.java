package moment.notification.domain;

public class DeepLinkGenerator {

    public static String generate(NotificationType notificationType, SourceData sourceData) {
        return switch (notificationType) {
            case NEW_COMMENT_ON_MOMENT -> {
                Long groupId = sourceData.getLong("groupId");
                Long momentId = sourceData.getLong("momentId");
                yield (groupId != null)
                    ? "/groups/" + groupId + "/moments/" + momentId
                    : "/moments/" + momentId;
            }
            case GROUP_JOIN_REQUEST, GROUP_JOIN_APPROVED ->
                "/groups/" + sourceData.getLong("groupId");
            case GROUP_KICKED -> null;
            case MOMENT_LIKED ->
                "/moments/" + sourceData.getLong("momentId");
            case COMMENT_LIKED ->
                "/comments/" + sourceData.getLong("commentId");
        };
    }
}
