import * as S from './index.styles';
import { PrivacyPolicyContent } from './PrivacyPolicyContent';

export default function PrivacyPolicyPage() {
  return (
    <S.PageWrapper>
      <S.Title>개인정보처리방침</S.Title>
      <S.LastUpdated>최종 수정일: 2025년 2월 24일</S.LastUpdated>
      <PrivacyPolicyContent />
    </S.PageWrapper>
  );
}
