import { useState } from 'react';
import { useJoinGroupMutation } from '@/features/group/api/useJoinGroupMutation';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { Button } from '@/shared/design-system/button/Button';
import { Input } from '@/shared/design-system/input/Input';
import * as S from './GroupJoinForm.styles';
import { useEffect } from 'react';

interface GroupJoinFormProps {
  initialCode?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function GroupJoinForm({ initialCode, onSuccess, onCancel }: GroupJoinFormProps) {
  const [inputVal, setInputVal] = useState(initialCode || '');
  const [nickname, setNickname] = useState('');
  const joinGroupMutation = useJoinGroupMutation();

  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const { data: profile } = useProfileQuery({ enabled: !!isLoggedIn });

  useEffect(() => {
    if (profile?.nickname && !nickname) {
      setNickname(profile.nickname);
    }
  }, [profile?.nickname]);

  const handleJoin = async (inviteCode: string) => {
    if (!nickname.trim()) return;

    try {
      await joinGroupMutation.mutateAsync({ inviteCode, nickname });
      onSuccess?.();
    } catch (error) {
      console.error('Failed to join group:', error);
    }
  };

  const handleSubmitCode = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputVal.trim()) return;

    // Extract code if it's a full URL
    let cleanCode = inputVal.trim();
    if (cleanCode.includes('/invite/')) {
      cleanCode = cleanCode.split('/invite/')[1].split('/')[0];
    }

    handleJoin(cleanCode);
  };

  return (
    <S.FormContainer>
      <S.Header>
        <S.Title>그룹 참여하기</S.Title>
        <S.Description>초대 코드를 입력하거나 링크를 붙여넣으세요.</S.Description>
      </S.Header>
      <S.Content as="form" onSubmit={handleSubmitCode}>
        <Input
          type="text"
          value={inputVal}
          onChange={e => setInputVal(e.target.value)}
          placeholder="초대 코드 또는 링크"
          aria-label="초대 코드 입력"
        />
        <Input
          type="text"
          value={nickname}
          onChange={e => setNickname(e.target.value)}
          placeholder="사용할 닉네임"
          aria-label="닉네임 입력"
        />
        <Button
          title={joinGroupMutation.isPending ? '참여 중...' : '참여하기'}
          variant="primary"
          type="submit"
          disabled={!inputVal.trim() || !nickname.trim() || joinGroupMutation.isPending}
        />
        <Button
          title="취소"
          variant="secondary"
          onClick={onCancel}
          type="button"
          disabled={joinGroupMutation.isPending}
        />
      </S.Content>
    </S.FormContainer>
  );
}
