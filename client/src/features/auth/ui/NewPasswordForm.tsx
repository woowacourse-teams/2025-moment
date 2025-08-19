import { Button } from '@/shared/ui';
import { Input } from '@/shared/ui/input/Input';
import * as S from './LoginForm.styles';

export const NewPasswordForm = () => {
  return (
    <S.LoginFormWrapper>
      <S.LoginFormContainer onSubmit={handleSubmit}>
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
                onChange={handleChange('email')}
              />
              <Button title="인증번호 전송" variant="primary" />
            </S.EmailCheckContainer>

            <S.ErrorMessage>{errors.email || ' '}</S.ErrorMessage>
          </S.InputGroup>
          <S.InputGroup>
            <S.Label htmlFor="password">인증번호 확인</S.Label>
            <Input
              id="password"
              type="password"
              placeholder="이메일에 전송된 인증번호를 입력해주세요"
              value={formData.password}
              onChange={handleChange('password')}
            />
            <S.ErrorMessage>{errors.password || ' '}</S.ErrorMessage>
          </S.InputGroup>
          <S.InputGroup>
            <S.Label htmlFor="password">비밀번호</S.Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력해주세요"
              value={formData.password}
              onChange={handleChange('password')}
            />
            <S.ErrorMessage>{errors.password || ' '}</S.ErrorMessage>
          </S.InputGroup>

          <S.InputGroup>
            <S.Label htmlFor="password">비밀번호 확인</S.Label>
            <Input
              id="password"
              type="password"
              placeholder="비밀번호 재입력해주세요"
              value={formData.password}
              onChange={handleChange('password')}
            />
            <S.ErrorMessage>{errors.password || ' '}</S.ErrorMessage>
          </S.InputGroup>
        </S.LoginFormContent>
        <S.LoginButton type="submit" disabled={isDisabled}>
          비밀번호 재발급
        </S.LoginButton>
      </S.LoginFormContainer>
    </S.LoginFormWrapper>
  );
};
