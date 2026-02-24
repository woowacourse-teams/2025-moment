import * as S from './index.styles';

const CONTACT_EMAIL = 'woowamoment@gmail.com';

export default function DeleteAccountPage() {
  return (
    <S.PageWrapper>
      <S.Title>계정 삭제 요청</S.Title>
      <S.Description>
        Moment 계정 및 관련 데이터를 삭제하려면 아래 이메일로 요청해 주세요. 요청 접수 후 7일 이내에
        처리됩니다.
      </S.Description>

      <S.InfoBox>
        <S.InfoLabel>문의 이메일</S.InfoLabel>
        <S.ContactEmail href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</S.ContactEmail>
      </S.InfoBox>

      <S.Notice>
        이메일 제목에 "계정 삭제 요청"을 포함하고, 가입 시 사용한 이메일 주소를 본문에 기재해
        주세요.
      </S.Notice>

      <S.Divider />

      <S.SectionTitle>삭제되는 데이터</S.SectionTitle>
      <S.List>
        <S.ListItem>계정 정보 (이메일, 프로필)</S.ListItem>
        <S.ListItem>작성한 모든 모멘트 및 댓글</S.ListItem>
        <S.ListItem>그룹 참여 기록</S.ListItem>
        <S.ListItem>기타 서비스 이용 기록</S.ListItem>
      </S.List>

      <S.Divider />

      <S.Notice>
        계정 삭제 후에는 데이터를 복구할 수 없습니다. 삭제를 원하지 않는 경우 앱 내 설정에서
        로그아웃만 진행하실 수 있습니다.
      </S.Notice>
    </S.PageWrapper>
  );
}
