import { useNewPassword } from '@/features/auth/hooks/useNewPassword';
import { Input } from '@/shared/ui/input/Input';
import * as S from './LoginForm.styles';

export const NewPasswordForm = () => {
  const { formData, isLoading, errors, updateNewPasswordForm, submitNewPasswordForm } =
    useNewPassword();

  return (
    <S.LoginFormWrapper>
      <S.LoginFormContainer onSubmit={submitNewPasswordForm}>
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
                value={formData.email}
                disabled
              />
            </S.EmailCheckContainer>
          </S.InputGroup>
          <S.InputGroup>
            <S.Label htmlFor="password">비밀번호</S.Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력해주세요"
              value={formData.newPassword}
              onChange={updateNewPasswordForm('newPassword')}
            />
            <S.ErrorMessage>{errors.newPassword || ' '}</S.ErrorMessage>
          </S.InputGroup>
          <S.InputGroup>
            <S.Label htmlFor="password">비밀번호 확인</S.Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호 재입력해주세요"
              value={formData.newPasswordCheck}
              onChange={updateNewPasswordForm('newPasswordCheck')}
            />
            <S.ErrorMessage>{errors.newPasswordCheck || ' '}</S.ErrorMessage>
          </S.InputGroup>
        </S.LoginFormContent>
        <S.LoginButton type="submit" disabled={isLoading}>
          비밀번호 재발급
        </S.LoginButton>
      </S.LoginFormContainer>
    </S.LoginFormWrapper>
  );
};
