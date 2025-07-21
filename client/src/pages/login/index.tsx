import { LoginForm } from '@/features/auth/ui/LoginForm';
import * as S from './index.styles';

export default function LoginPage() {
  return (
    <S.LoginPageWrapper>
      <LoginForm />
    </S.LoginPageWrapper>
  );
}
