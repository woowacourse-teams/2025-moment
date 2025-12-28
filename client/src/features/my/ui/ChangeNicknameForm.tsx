import { useRandomNicknameQuery } from '@/features/auth/api/useRandomNicknameQuery';
import { useEnterKeyHandler } from '@/shared/hooks';
import { Input } from '@/shared/design-system/input/Input';
import { RotateNicknameButton } from '@/widgets/signup/SignupStep2';
import { RotateCw } from 'lucide-react';
import { useEffect } from 'react';
import { useChangeNicknameMutation } from '../api/useChangeNicknameMutation';
import * as S from './ChangeNicknameForm.styles';
import { Button } from '@/shared/design-system/button';

export const ChangeNicknameForm = ({
  nickname,
  updateNickname,
  handleCloseNicknameModal,
}: {
  nickname: string;
  updateNickname: (nickname: string) => void;
  handleCloseNicknameModal: () => void;
}) => {
  const { data: randomNickname, refetch } = useRandomNicknameQuery();
  const { mutate: changeNickname } = useChangeNicknameMutation();
  useEnterKeyHandler(refetch);

  useEffect(() => {
    if (randomNickname) {
      updateNickname(randomNickname);
    }
  }, [randomNickname]);

  const handleRotateNickname = () => {
    refetch();
  };

  const handleSubmit = () => {
    changeNickname({ newNickname: nickname });
    handleCloseNicknameModal();
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
      </S.InputGroup>
      <Button title="변경하기" variant="primary" onClick={handleSubmit} />
    </S.ChangeNicknameFormWrapper>
  );
};
