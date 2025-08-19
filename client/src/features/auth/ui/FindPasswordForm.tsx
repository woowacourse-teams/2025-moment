import { useFindPasswordEmail } from '@/features/auth/hooks/useFindPasswordEmail';
import { Input } from '@/shared/ui/input/Input';
import * as S from './LoginForm.styles';

export const FindPasswordForm = () => {
  const { email, isLoading, error, updateEmail, submitFindPasswordEmail } = useFindPasswordEmail();
  return (
    <S.LoginFormWrapper>
      <S.LoginFormContainer onSubmit={submitFindPasswordEmail}>
        <S.LoginFormTitleContainer>
          <S.LoginLogoTitleContainer>
            <S.LogoImage src="/images/logo.webp" alt="" />
            <S.LogoTitle>모멘트</S.LogoTitle>
          </S.LoginLogoTitleContainer>
          <S.LoginTitle>비밀번호 재발급</S.LoginTitle>
        </S.LoginFormTitleContainer>
        <S.LoginFormContent>
          <S.InputGroup>
            <S.Label htmlFor="email">이메일</S.Label>
            <S.EmailCheckContainer>
              <Input
                id="email"
                type="email"
                placeholder="이메일을 입력해주세요"
                value={email}
                onChange={updateEmail}
              />
            </S.EmailCheckContainer>
            <S.ErrorMessage>{error}</S.ErrorMessage>
          </S.InputGroup>
        </S.LoginFormContent>
        <S.LoginButton type="submit" disabled={isLoading && error !== ''}>
          인증 링크 전송하기
        </S.LoginButton>
      </S.LoginFormContainer>
    </S.LoginFormWrapper>
  );
};
