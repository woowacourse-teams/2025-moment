package moment.user.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MomentRandomNicknameGenerator implements NicknameGenerator {

    private static final List<String> ADJECTIVES = Arrays.asList(
            "꿈꾸는", "포근한", "반짝반짝", "따스한", "오로라빛", "신비로운",
            "다정한", "눈부신", "아련한", "고요한", "영롱한", "찬란한",
            "온화한", "행복한", "은은한", "투명한", "소중한", "나른한",
            "미소짓는", "순수한", "애틋한", "평온한", "물빛의", "장밋빛",
            "하늘빛", "핑크빛", "보랏빛", "새벽녘", "해질녘", "그리운"
    );

    private static final List<String> LINKING_WORDS = Arrays.asList(
            "우주", "은하", "성운", "하늘", "바다", "호수", "정원", "마음",
            "세상", "도시", "새벽", "저녁", "순간", "찰나", "여름", "겨울",
            "기억", "추억", "여행", "항해", "비행", "산책", "노래", "편지",
            "선물", "약속", "기도", "주문", "파동", "울림", "조각", "기적",
            "운명", "향기", "빛깔", "소리", "온기", "꿈결", "숨결", "물결"
    );

    private static final List<String> NOUNS = Arrays.asList(
            "시리우스", "베가", "리라", "스피카", "미라", "데네브", "알타이르",
            "카펠라", "리겔", "알골", "레오", "드라코", "폴룩스", "레굴루스",
            "수성", "금성", "화성", "목성", "토성", "천왕성", "해왕성",
            "루나", "달", "이오", "타이탄", "트리톤", "카론", "포보스", "데이모스",
            "아리엘", "레아", "미마스", "오베론"
    );

    private static final String NICKNAME_FORMAT = "%s %s의 %s";

    private final Random random = new Random();

    @Override
    public String generateNickname() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String linkingWord = LINKING_WORDS.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(ADJECTIVES.size()));

        return String.format(NICKNAME_FORMAT, adjective, linkingWord, noun);
    }
}
