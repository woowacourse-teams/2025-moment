import { useRandomNicknameQuery } from '@/features/auth/api/useRandomNicknameQuery';
import { SignupFormData } from '@/features/auth/types/signup';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import { Input } from '@/shared/ui/input/Input';
import styled from '@emotion/styled';
import { RotateCw } from 'lucide-react';
import { useEffect } from 'react';
import * as S from './SignupStep.styles';

interface SignupStep2Props {
  signupData: SignupFormData;
  onNext?: () => void;
  updateNickname: (nickname: string) => void;
}

export const SignupStep2 = ({ signupData, onNext, updateNickname }: SignupStep2Props) => {
  useEnterKeyHandler(onNext);
  const { data: randomNickname, isError, error, refetch } = useRandomNicknameQuery();

  useEffect(() => {
    if (randomNickname) {
      updateNickname(randomNickname);
    }
  }, [randomNickname]);

  if (isError) {
    console.error('Error fetching random nickname:', error);
  }

  const handleRotateNickname = () => {
    refetch();
  };

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
          <RotateNicknameButton onClick={handleRotateNickname}>
            <RotateCw size={25} color="white" />
          </RotateNicknameButton>
        </S.CheckExistContainer>
        {isError && (
          <S.ErrorMessage>닉네임을 가져오는 데 실패했습니다. 다시 시도해주세요.</S.ErrorMessage>
        )}
      </S.InputGroup>
    </S.StepContainer>
  );
};

export const RotateNicknameButton = styled.button`
  background-color: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    scale: 1.1;
    transition: scale 0.1s ease-in-out;
  }
`;
