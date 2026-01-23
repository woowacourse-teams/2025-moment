import { useParams, useNavigate } from 'react-router';
import { useInviteInfoQuery } from '@/features/group/api/useInviteInfoQuery';
import { useJoinGroupMutation } from '@/features/group/api/useJoinGroupMutation';
import { Button } from '@/shared/design-system/button/Button';
import { ROUTES } from '@/app/routes/routes';
import * as S from './JoinGroupPage.styles';

export default function JoinGroupPage() {
  const { code } = useParams<{ code: string }>();
  const navigate = useNavigate();
  const { data: inviteInfo, isLoading, error } = useInviteInfoQuery(code || '');
  const joinGroupMutation = useJoinGroupMutation();

  const handleJoinGroup = async () => {
    if (!code) return;

    try {
      await joinGroupMutation.mutateAsync({ code });
      navigate(ROUTES.GROUPS);
    } catch (error) {
      console.error('Failed to join group:', error);
    }
  };

  const handleGoBack = () => {
    navigate(ROUTES.GROUPS);
  };

  if (isLoading) {
    return (
      <S.PageContainer>
        <S.LoadingState>초대 정보를 불러오는 중...</S.LoadingState>
      </S.PageContainer>
    );
  }

  if (error || !inviteInfo?.data.isValid) {
    return (
      <S.PageContainer>
        <S.ErrorState>
          <S.ErrorText>유효하지 않은 초대 코드입니다.</S.ErrorText>
          <Button title="그룹 목록으로" variant="primary" onClick={handleGoBack} />
        </S.ErrorState>
      </S.PageContainer>
    );
  }

  const info = inviteInfo.data;

  return (
    <S.PageContainer>
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
          aria-label="그룹 가입 신청"
        />
        <Button title="취소" variant="secondary" onClick={handleGoBack} aria-label="취소" />
      </S.Content>
    </S.PageContainer>
  );
}
