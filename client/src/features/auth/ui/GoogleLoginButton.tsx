import * as S from './GoogleLoginButton.styles';

interface GoogleLoginButtonProps {
  onClick: () => void;
}

export const GoogleLoginButton = ({ onClick }: GoogleLoginButtonProps) => {
  return (
    <S.GoogleLoginButton onClick={onClick}>
      <S.GoogleLoginButtonIcon src="/google.png" alt="google" />
      Sign in with Google
    </S.GoogleLoginButton>
  );
};
