import * as S from './index.styles';
import { TermsContent } from './TermsContent';

export default function TermsPage() {
  return (
    <S.TermsPageWrapper>
      <S.Title>이용약관</S.Title>
      <TermsContent />
    </S.TermsPageWrapper>
  );
}
