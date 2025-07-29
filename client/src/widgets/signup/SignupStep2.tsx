import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { Input } from '@/shared/ui/input/Input';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import * as S from './SignupStep.styles';
import { CheckButton } from '@/features/auth/ui/CheckButton';

interface SignupStep2Props {
  signupData: SignupFormData;
  errors: SignupErrors;
  handleChange: (field: keyof SignupFormData) => (e: React.ChangeEvent<HTMLInputElement>) => void;
  onNext?: () => void;
  handleCheckNickname: (value: string) => void;
  nicknameErrorMessage: string;
  isNicknameChecked: boolean;
}

export const SignupStep2 = ({
  signupData,
  errors,
  handleChange,
  onNext,
  handleCheckNickname,
  nicknameErrorMessage,
  isNicknameChecked,
}: SignupStep2Props) => {
  useEnterKeyHandler(onNext);

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="nickname">닉네임</S.Label>
        <S.CheckExistContainer>
          <Input
            id="nickname"
            type="text"
            placeholder="닉네임을 입력해주세요"
            value={signupData.nickname}
            onChange={handleChange('nickname')}
          />
          <CheckButton onClick={() => handleCheckNickname(signupData.nickname)} />
        </S.CheckExistContainer>
        {isNicknameChecked && !nicknameErrorMessage ? (
          <S.SuccessMessage>사용 가능한 닉네임입니다.</S.SuccessMessage>
        ) : (
          <S.ErrorMessage>{nicknameErrorMessage || errors.nickname}</S.ErrorMessage>
        )}
      </S.InputGroup>
    </S.StepContainer>
  );
};
