import { LoginForm } from "@/features/auth/ui/LoginForm";
import * as S from "./LoginPage.styles";

export default function LoginPage() {
  return (
    <S.Container>
      <S.BackgroundOverlay />
      <LoginForm />
    </S.Container>
  );
}
