import { useCheckEmailCode } from '@/features/auth/hooks/useCheckEmailCode';
import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { CheckButton } from '@/features/auth/ui/CheckButton';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import { Input } from '@/shared/ui/input/Input';
import * as S from './SignupStep.styles';

interface SignupStep1Props {
  signupData: SignupFormData;
  errors: SignupErrors;
  handleChange: (field: keyof SignupFormData) => (e: React.ChangeEvent<HTMLInputElement>) => void;
  onNext?: () => void;
  handleCheckEmail: (value: string) => void;
  emailErrorMessage: string;
  isEmailChecked: boolean;
  isEmailCheckLoading: boolean;
}

export const SignupStep1 = ({
  signupData,
  errors,
  handleChange,
  onNext,
  handleCheckEmail,
  emailErrorMessage,
  isEmailChecked,
  isEmailCheckLoading,
}: SignupStep1Props) => {
  const {
    emailCode,
    isCheckEmailCodeLoading,
    isCheckEmailCodeError,
    isCheckEmailCodeSuccess,
    updateEmailCode,
    checkEmailCode,
  } = useCheckEmailCode();
  useEnterKeyHandler(onNext);

  return (
    <S.StepContainer>
      <S.EmailGroup>
        <S.EmailLabel htmlFor="username">이메일</S.EmailLabel>
        <S.CheckExistContainer>
          <Input
            id="email"
            type="email"
            placeholder="이메일을 입력해주세요"
            value={signupData.email}
            onChange={handleChange('email')}
          />
          <CheckButton
            title="인증코드 전송"
            onClick={() => handleCheckEmail(signupData.email)}
            disabled={isEmailCheckLoading || errors.email !== ''}
          />
        </S.CheckExistContainer>
        {isEmailChecked && !emailErrorMessage ? (
          <S.SuccessMessage>이메일 중복 확인에 성공했습니다.</S.SuccessMessage>
        ) : (
          <S.ErrorMessage>{emailErrorMessage || errors.email}</S.ErrorMessage>
        )}
        <S.CheckExistContainer>
          <Input
            id="email"
            type="email"
            placeholder="이메일로 전송된 인증코드를 입력해주세요"
            value={emailCode}
            onChange={updateEmailCode}
          />
          <CheckButton
            title="인증코드 확인"
            onClick={() => checkEmailCode(signupData.email, emailCode)}
            disabled={isCheckEmailCodeLoading}
          />
        </S.CheckExistContainer>
        {isCheckEmailCodeSuccess ? (
          <S.SuccessMessage>인증코드가 확인되었습니다.</S.SuccessMessage>
        ) : emailCode !== '' && isCheckEmailCodeError ? (
          <S.ErrorMessage>인증코드가 일치하지 않습니다.</S.ErrorMessage>
        ) : (
          <S.ErrorMessage></S.ErrorMessage>
        )}
      </S.EmailGroup>

      <S.InputGroup>
        <S.Label htmlFor="password">비밀번호</S.Label>
        <Input
          id="password"
          type="password"
          placeholder="비밀번호를 입력해주세요"
          value={signupData.password}
          onChange={handleChange('password')}
        />

        <S.ErrorMessage>{errors.password || ''}</S.ErrorMessage>
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="rePassword">비밀번호 확인</S.Label>
        <Input
          id="rePassword"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          value={signupData.rePassword}
          onChange={handleChange('rePassword')}
        />

        <S.ErrorMessage>{errors.rePassword || ''}</S.ErrorMessage>
      </S.InputGroup>
    </S.StepContainer>
  );
};
