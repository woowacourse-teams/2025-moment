import { useRandomNicknameQuery } from '@/features/auth/hooks/useRandomNicknameQuery';
import { SignupFormData } from '@/features/auth/types/signup';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import { Input } from '@/shared/ui/input/Input';
import { useEffect } from 'react';
import * as S from './SignupStep.styles';

interface SignupStep2Props {
  signupData: SignupFormData;
  onNext?: () => void;
  updateNickname: (nickname: string) => void;
}

export const SignupStep2 = ({ signupData, onNext, updateNickname }: SignupStep2Props) => {
  useEnterKeyHandler(onNext);
  const { data: randomNickname, isError, error } = useRandomNicknameQuery();

  useEffect(() => {
    if (randomNickname) {
      updateNickname(randomNickname);
    }
  }, [randomNickname]);

  if (isError) {
    console.error('Error fetching random nickname:', error);
  }

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
            disabled
          />
        </S.CheckExistContainer>
        {isError && (
          <S.ErrorMessage>닉네임을 가져오는 데 실패했습니다. 다시 시도해주세요.</S.ErrorMessage>
        )}
      </S.InputGroup>
    </S.StepContainer>
  );
};
