import * as S from './index.styles';

export const PrivacyPolicyContent = () => {
  return (
    <>
      <S.Section>
        <S.Paragraph>
          Moment(이하 "서비스")은 이용자의 개인정보를 중요하게 생각하며, 「개인정보 보호법」 및 관련
          법령을 준수합니다. 본 개인정보처리방침은 서비스가 어떤 정보를 수집하고, 어떻게 이용하며,
          어떻게 보호하는지 안내합니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제1조 (수집하는 개인정보 항목)</S.SectionTitle>
        <S.Paragraph>서비스는 다음과 같은 정보를 수집할 수 있습니다.</S.Paragraph>
        <S.List>
          <S.ListItem>
            계정 정보: Google 소셜 로그인을 통해 제공되는 이메일 주소 및 프로필 정보
          </S.ListItem>
          <S.ListItem>
            이용 기록: 서비스 내 활동 기록(페이지 조회, 게시물 작성 등), 접속 일시
          </S.ListItem>
          <S.ListItem>기기 정보: 기기 종류, 운영체제 버전, 앱 버전</S.ListItem>
        </S.List>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제2조 (개인정보의 이용 목적)</S.SectionTitle>
        <S.Paragraph>수집한 개인정보는 다음 목적을 위해 이용됩니다.</S.Paragraph>
        <S.List>
          <S.ListItem>회원 가입 및 계정 인증, 서비스 제공</S.ListItem>
          <S.ListItem>서비스 개선 및 신규 기능 개발</S.ListItem>
          <S.ListItem>부정 이용 방지 및 보안 유지</S.ListItem>
          <S.ListItem>이용 통계 분석 및 서비스 품질 향상</S.ListItem>
        </S.List>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제3조 (개인정보의 제3자 제공)</S.SectionTitle>
        <S.Paragraph>
          서비스는 이용자의 개인정보를 외부에 판매하거나 임의로 제공하지 않습니다.
        </S.Paragraph>
        <S.Paragraph>
          다만, 서비스 운영을 위해 신뢰할 수 있는 외부 서비스 제공업체와 제한적으로 정보를 공유할 수
          있습니다.
        </S.Paragraph>
        <S.List>
          <S.ListItem>Google LLC — 소셜 로그인 인증 (Google OAuth)</S.ListItem>
          <S.ListItem>Google LLC — 이용 통계 분석 (Google Analytics)</S.ListItem>
        </S.List>
        <S.Paragraph>위 업체들은 각자의 개인정보처리방침에 따라 데이터를 처리합니다.</S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제4조 (개인정보의 보유 및 이용 기간)</S.SectionTitle>
        <S.Paragraph>
          서비스는 이용자의 개인정보를 서비스 제공 기간 동안 보유하며, 회원 탈퇴 또는 이용 목적 달성
          시 지체 없이 파기합니다. 단, 관련 법령에 의해 보존이 필요한 경우 해당 기간 동안
          보관합니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제5조 (개인정보의 파기)</S.SectionTitle>
        <S.Paragraph>
          보유 기간이 만료되거나 이용 목적이 달성된 개인정보는 복구할 수 없는 방법으로 안전하게
          파기합니다. 전자적 파일 형태의 정보는 기술적 방법을 사용하여 삭제하며, 출력물 등은 분쇄
          또는 소각하여 파기합니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제6조 (개인정보 보호 조치)</S.SectionTitle>
        <S.Paragraph>
          서비스는 이용자의 개인정보를 보호하기 위해 다음과 같은 기술적·관리적 조치를 취하고
          있습니다.
        </S.Paragraph>
        <S.List>
          <S.ListItem>개인정보의 암호화 저장 및 전송</S.ListItem>
          <S.ListItem>해킹 등 외부 침입에 대비한 보안 시스템 운영</S.ListItem>
          <S.ListItem>개인정보 접근 권한 최소화</S.ListItem>
        </S.List>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제7조 (이용자의 권리)</S.SectionTitle>
        <S.Paragraph>이용자는 언제든지 다음과 같은 권리를 행사할 수 있습니다.</S.Paragraph>
        <S.List>
          <S.ListItem>본인 개인정보에 대한 열람 요청</S.ListItem>
          <S.ListItem>오류가 있는 개인정보에 대한 정정 요청</S.ListItem>
          <S.ListItem>개인정보 삭제 및 처리 정지 요청</S.ListItem>
        </S.List>
        <S.Paragraph>권리 행사는 아래 연락처로 요청하시면 지체 없이 처리하겠습니다.</S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>제8조 (개인정보처리방침의 변경)</S.SectionTitle>
        <S.Paragraph>
          본 개인정보처리방침은 법령 또는 서비스 변경에 따라 내용이 수정될 수 있습니다. 변경 시 앱
          내 공지 또는 본 페이지를 통해 안내드립니다.
        </S.Paragraph>
      </S.Section>

      <S.Section>
        <S.SectionTitle>문의</S.SectionTitle>
        <S.Paragraph>개인정보 처리에 관한 문의사항은 아래 이메일로 연락해 주세요.</S.Paragraph>
        <S.Paragraph>
          이메일:{' '}
          <S.ContactEmail href="mailto:woowamoment@gmail.com">woowamoment@gmail.com</S.ContactEmail>
        </S.Paragraph>
      </S.Section>
    </>
  );
};
