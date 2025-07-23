import { SignupErrors, SignupFormData } from '@/features/auth/types/signup';
import { Input } from '@/shared/ui/input/Input';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import * as S from './SignupStep.styles';

interface SignupStep1Props {
  signupData: SignupFormData;
  errors: SignupErrors;
  handleChange: (field: keyof SignupFormData) => (e: React.ChangeEvent<HTMLInputElement>) => void;
  onNext?: () => void;
}

export const SignupStep1 = ({ signupData, errors, handleChange, onNext }: SignupStep1Props) => {
  useEnterKeyHandler(onNext);

  return (
    <S.StepContainer>
      <S.InputGroup>
        <S.Label htmlFor="username">이메일</S.Label>
        <Input
          id="email"
          type="email"
          placeholder="이메일을 입력해주세요"
          value={signupData.email}
          onChange={handleChange('email')}
        />
        <S.ErrorMessage>{errors.email || ''}</S.ErrorMessage>
      </S.InputGroup>

      <S.InputGroup>
        <S.Label htmlFor="password">비밀번호</S.Label>
        <Input
          id="password"
          type="password"
          placeholder="비밀번호를 입력해주세요"
          value={signupData.password}
          onChange={handleChange('password')}
        />
        {/* Todo: 비밀번호 오류 처리는 추후 유효성 검사 로직 포함하여 처리 */}
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
        {/* Todo: 비밀번호 오류 처리는 추후 유효성 검사 로직 포함하여 처리 */}
        <S.ErrorMessage>{errors.rePassword || ''}</S.ErrorMessage>
      </S.InputGroup>
    </S.StepContainer>
  );
};
