import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { CheckButton } from '@/features/auth/ui/CheckButton';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import { Input } from '@/shared/design-system/input/Input';
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
  emailCode: string;
  isCheckEmailCodeLoading: boolean;
  isCheckEmailCodeError: boolean;
  isCheckEmailCodeSuccess: boolean;
  updateEmailCode: (e: React.ChangeEvent<HTMLInputElement>) => void;
  checkEmailCode: (email: string, code: string) => void;
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
  emailCode,
  isCheckEmailCodeLoading,
  isCheckEmailCodeError,
  isCheckEmailCodeSuccess,
  updateEmailCode,
  checkEmailCode,
}: SignupStep1Props) => {
  useEnterKeyHandler(onNext);

  return (
    <S.StepContainer>
      <S.EmailGroup>
        <S.EmailLabel htmlFor="email">이메일</S.EmailLabel>
        <S.CheckExistContainer>
          <Input
            id="email"
            type="email"
            placeholder="이메일을 입력해주세요"
            value={signupData.email}
            onChange={handleChange('email')}
            aria-describedby="email-error"
          />
          <CheckButton
            title="인증코드 전송"
            onClick={() => handleCheckEmail(signupData.email)}
            disabled={isEmailCheckLoading || errors.email !== '' || isCheckEmailCodeSuccess}
            aria-label="이메일 인증코드 전송"
          />
        </S.CheckExistContainer>
        {isEmailChecked && !emailErrorMessage ? (
          <S.SuccessMessage role="status" aria-live="polite">
            인증코드가 전송되었습니다.
          </S.SuccessMessage>
        ) : (
          <S.ErrorMessage id="email-error" role="alert" aria-live="assertive">
            {emailErrorMessage || errors.email}
          </S.ErrorMessage>
        )}
        <S.CheckExistContainer>
          <Input
            id="emailCode"
            type="text"
            placeholder="이메일로 전송된 인증코드를 입력해주세요"
            value={emailCode}
            onChange={updateEmailCode}
            aria-describedby="emailCode-error"
          />
          <CheckButton
            title="인증코드 확인"
            onClick={() => checkEmailCode(signupData.email, emailCode)}
            disabled={isCheckEmailCodeSuccess || isCheckEmailCodeLoading || emailCode.length !== 6}
            aria-label="이메일 인증코드 확인"
          />
        </S.CheckExistContainer>
        {isCheckEmailCodeSuccess ? (
          <S.SuccessMessage role="status" aria-live="polite">
            인증코드가 확인되었습니다.
          </S.SuccessMessage>
        ) : emailCode !== '' && isCheckEmailCodeError ? (
          <S.ErrorMessage id="emailCode-error" role="alert" aria-live="assertive">
            인증코드가 일치하지 않습니다.
          </S.ErrorMessage>
        ) : (
          <S.ErrorMessage id="emailCode-error"></S.ErrorMessage>
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
          aria-describedby="password-error"
        />

        <S.ErrorMessage id="password-error" role="alert" aria-live="polite">
          {errors.password || ''}
        </S.ErrorMessage>
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="rePassword">비밀번호 확인</S.Label>
        <Input
          id="rePassword"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          value={signupData.rePassword}
          onChange={handleChange('rePassword')}
          aria-describedby="rePassword-error"
        />

        <S.ErrorMessage id="rePassword-error" role="alert" aria-live="polite">
          {errors.rePassword || ''}
        </S.ErrorMessage>
      </S.InputGroup>
    </S.StepContainer>
  );
};
