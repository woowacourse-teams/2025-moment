import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { Input } from '@/shared/ui/input/Input';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import * as S from './SignupStep.styles';

interface SignupStep2Props {
  signupData: SignupFormData;
  errors: SignupErrors;
  handleChange: (field: keyof SignupFormData) => (e: React.ChangeEvent<HTMLInputElement>) => void;
  onNext?: () => void;
}

export const SignupStep2 = ({ signupData, errors, handleChange, onNext }: SignupStep2Props) => {
  useEnterKeyHandler(onNext);

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="nickname">닉네임</S.Label>
        <Input
          id="nickname"
          type="text"
          placeholder="닉네임을 입력해주세요"
          value={signupData.nickname}
          onChange={handleChange('nickname')}
        />
        <S.ErrorMessage>{errors.nickname || ''}</S.ErrorMessage>
      </S.InputGroup>
    </S.StepContainer>
  );
};
