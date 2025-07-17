import { SignupForm } from '@/features/auth/ui/SignupForm';
import * as S from './index.styles';

export default function SignupPage() {
  return (
    <S.SignupPageWrapper>
      <SignupForm />
    </S.SignupPageWrapper>
  );
}
