import { useState } from 'react';
import { useInviteInfoQuery } from '@/features/group/api/useInviteInfoQuery';
import { useJoinGroupMutation } from '@/features/group/api/useJoinGroupMutation';
import { Button } from '@/shared/design-system/button/Button';
import { Input } from '@/shared/design-system/input/Input';
import * as S from './GroupJoinForm.styles';

interface GroupJoinFormProps {
  initialCode?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function GroupJoinForm({ initialCode, onSuccess, onCancel }: GroupJoinFormProps) {
  const [code, setCode] = useState<string>(initialCode || '');
  const [inputVal, setInputVal] = useState('');

  const { data: inviteInfo, isLoading, error } = useInviteInfoQuery(code || '');
  const joinGroupMutation = useJoinGroupMutation();

  const handleJoinGroup = async () => {
    if (!code) return;

    try {
      await joinGroupMutation.mutateAsync({ code });
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

    setCode(cleanCode);
  };

  const handleReset = () => {
    setCode('');
    setInputVal('');
  };

  // Case 1: No code provided (Input Mode)
  if (!code) {
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
          <Button title="확인" variant="primary" type="submit" disabled={!inputVal.trim()} />
          <Button title="취소" variant="secondary" onClick={onCancel} type="button" />
        </S.Content>
      </S.FormContainer>
    );
  }

  // Case 2: Loading info
  if (isLoading) {
    return (
      <S.FormContainer>
        <S.LoadingState>초대 정보를 불러오는 중...</S.LoadingState>
      </S.FormContainer>
    );
  }

  // Case 3: Error or Invalid
  if (error || !inviteInfo?.data.isValid) {
    return (
      <S.FormContainer>
        <S.ErrorState>
          <S.ErrorText>유효하지 않은 초대 코드입니다.</S.ErrorText>
          <Button title="다시 입력하기" variant="primary" onClick={handleReset} />
          <Button title="취소" variant="secondary" onClick={onCancel} />
        </S.ErrorState>
      </S.FormContainer>
    );
  }

  const info = inviteInfo.data;

  // Case 4: Valid, Show Info & Join Button
  return (
    <S.FormContainer>
      <S.Header>
        <S.Title>{info.groupName}</S.Title>
        <S.Description>{info.description || '그룹에 대한 설명이 없습니다.'}</S.Description>
      </S.Header>

      <S.Content>
        <S.Description>멤버 {info.memberCount}명</S.Description>

        <Button
          title="그룹 가입 신청"
          variant="primary"
          onClick={handleJoinGroup}
          disabled={joinGroupMutation.isPending}
        />
        <Button title="취소" variant="secondary" onClick={onCancel} />
      </S.Content>
    </S.FormContainer>
  );
}
