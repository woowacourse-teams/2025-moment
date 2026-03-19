package moment.global.page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;

public record Cursor(String cursor) {

    private static final String CURSOR_PART_DELIMITER = "_";
    private static final int CURSOR_TIME_INDEX = 0;
    private static final int CURSOR_ID_INDEX = 1;

    public Cursor {
        if (cursor != null) {
            String[] split = cursor.split(CURSOR_PART_DELIMITER);
            if (split.length < 1 || split.length > 2) {
                throw new MomentException(ErrorCode.CURSOR_INVALID);
            }
            try {
                LocalDateTime.parse(cursor.split(CURSOR_PART_DELIMITER)[CURSOR_TIME_INDEX]);
                Long.valueOf(cursor.split(CURSOR_PART_DELIMITER)[CURSOR_ID_INDEX]);
            } catch (Exception e) {
                throw new MomentException(ErrorCode.CURSOR_INVALID);
            }
        }
    }

    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }

    public boolean isNotFirstPage() {
        return cursor != null;
    }

    public LocalDateTime dateTime() {
        return LocalDateTime.parse(cursor.split(CURSOR_PART_DELIMITER)[CURSOR_TIME_INDEX]);
    }

    public Long id() {
        return Long.valueOf(cursor.split(CURSOR_PART_DELIMITER)[CURSOR_ID_INDEX]);
    }

    public String extract(List<Cursorable> values, boolean hasNext) {
        String nextCursor = null;

        List<Cursorable> copyValues = new ArrayList<>(values);

        if (!copyValues.isEmpty() && hasNext) {
            Cursorable cursorTargetValue = copyValues.get(values.size() - 2);
            nextCursor =
                    cursorTargetValue.getCreatedAt().toString() + CURSOR_PART_DELIMITER + cursorTargetValue.getId();
        }

        return nextCursor;
    }
}
