import { useRandomNicknameQuery } from '@/features/auth/hooks/useRandomNicknameQuery';
import { useChangeNicknameMutation } from '../hooks/useChangeNicknameMutation';
import { useEnterKeyHandler } from '@/shared/hooks';
import { useEffect } from 'react';
import * as S from './ChangeNicknameForm.styles';
import { RotateNicknameButton } from '@/widgets/signup/SignupStep2';
import { RotateCw } from 'lucide-react';
import { Input } from '@/shared/ui/input/Input';
import { Button } from '@/shared/ui';

export const ChangeNicknameForm = ({
  nickname,
  updateNickname,
}: {
  nickname: string;
  updateNickname: (nickname: string) => void;
}) => {
  const { data: randomNickname, isError, refetch } = useRandomNicknameQuery();
  const { mutate: changeNickname } = useChangeNicknameMutation();
  useEnterKeyHandler(refetch);

  useEffect(() => {
    if (randomNickname) {
      updateNickname(randomNickname);
    }
  }, [randomNickname]);

  if (isError) {
    console.error('닉네임 변경 실패');
  }

  const handleRotateNickname = () => {
    refetch();
  };

  const handleSubmit = () => {
    changeNickname({ newNickname: nickname });
  };
  return (
    <S.ChangeNicknameFormWrapper>
      <S.InputGroup>
        <S.ChangeNicknameContent>
          <Input
            id="nickname"
            type="text"
            placeholder="닉네임을 입력해주세요"
            value={nickname}
            disabled
          />
          <RotateNicknameButton onClick={handleRotateNickname}>
            <RotateCw size={25} color="white" />
          </RotateNicknameButton>
        </S.ChangeNicknameContent>
        {isError && (
          <S.ErrorMessage>닉네임을 가져오는 데 실패했습니다. 다시 시도해주세요.</S.ErrorMessage>
        )}
      </S.InputGroup>
      <Button title="변경하기" variant="primary" onClick={handleSubmit} />
    </S.ChangeNicknameFormWrapper>
  );
};
