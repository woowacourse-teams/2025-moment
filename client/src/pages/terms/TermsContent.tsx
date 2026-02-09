import * as S from './index.styles';

export const TermsContent = () => {
  return (
    <>
      <S.Section>
        <S.SectionTitle>제1조 (목적)</S.SectionTitle>
        <S.Paragraph>
          본 약관은 Moment(이하 "서비스")의 이용 조건 및 절차, 이용자와 서비스 제공자의 권리, 의무 및
          책임사항을 규정하는 것을 목적으로 합니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제2조 (용어의 정의)</S.SectionTitle>
        <S.Paragraph>
          "이용자"란 본 약관에 동의하고 서비스를 이용하는 자를 의미합니다. "콘텐츠"란 이용자가 서비스
          내에서 작성, 업로드하는 텍스트, 이미지 등 모든 형태의 정보를 의미합니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제3조 (콘텐츠 이용 규칙)</S.SectionTitle>
        <S.Paragraph>이용자는 서비스를 이용함에 있어 다음 각 호의 행위를 하여서는 안 됩니다.</S.Paragraph>
        <S.List>
          <S.ListItem>음란물 또는 성적으로 부적절한 콘텐츠를 게시하는 행위</S.ListItem>
          <S.ListItem>혐오 발언, 차별, 비하 등의 콘텐츠를 게시하는 행위</S.ListItem>
          <S.ListItem>폭력적이거나 위협적인 콘텐츠를 게시하는 행위</S.ListItem>
          <S.ListItem>타인의 개인정보를 무단으로 수집하거나 공개하는 행위</S.ListItem>
          <S.ListItem>스팸, 광고, 허위 정보를 유포하는 행위</S.ListItem>
          <S.ListItem>타인을 괴롭히거나 스토킹하는 행위</S.ListItem>
          <S.ListItem>법률에 위반되는 콘텐츠를 게시하는 행위</S.ListItem>
        </S.List>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제4조 (제재 조치)</S.SectionTitle>
        <S.Paragraph>
          서비스는 본 약관을 위반한 이용자에 대해 다음과 같은 제재 조치를 취할 수 있습니다.
        </S.Paragraph>
        <S.List>
          <S.ListItem>위반 콘텐츠의 삭제 또는 비공개 처리</S.ListItem>
          <S.ListItem>서비스 이용 제한 (일시 정지)</S.ListItem>
          <S.ListItem>계정 영구 정지 또는 삭제</S.ListItem>
          <S.ListItem>관련 법률에 따른 법적 조치</S.ListItem>
        </S.List>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제5조 (신고 및 차단)</S.SectionTitle>
        <S.Paragraph>
          이용자는 부적절한 콘텐츠 또는 악성 이용자를 서비스 내 신고 기능을 통해 신고할 수 있습니다.
          또한 원치 않는 이용자를 차단하여 해당 이용자의 콘텐츠가 표시되지 않도록 할 수 있습니다.
        </S.Paragraph>
        <S.Paragraph>
          서비스는 신고된 콘텐츠 및 이용자에 대해 검토 후 적절한 조치를 취합니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제6조 (면책 조항)</S.SectionTitle>
        <S.Paragraph>
          서비스는 이용자 간의 분쟁에 대해 개입할 의무를 지지 않으며, 이용자가 게시한 콘텐츠에 대한
          책임은 해당 콘텐츠를 게시한 이용자에게 있습니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제7조 (약관의 변경)</S.SectionTitle>
        <S.Paragraph>
          서비스는 필요한 경우 본 약관을 변경할 수 있으며, 변경된 약관은 서비스 내 공지를 통해
          효력이 발생합니다.
        </S.Paragraph>
      </S.Section>
    </>
  );
};
