import { useParams, useNavigate } from 'react-router';
import { useGroupDetailQuery } from '@/features/group/api/useGroupDetailQuery';
import { useGroupOwnership } from '@/features/group/hooks/useGroupOwnership';
import { GroupInviteSection } from '@/features/group/ui/GroupInviteSection';
import { Button } from '@/shared/design-system/button/Button';
import { ROUTES } from '@/app/routes/routes';
import * as S from './GroupDetailPage.styles';

export default function GroupDetailPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { data: groupData, isLoading } = useGroupDetailQuery(groupId || '');
  const { isOwner } = useGroupOwnership(groupId);

  const handleGoToMembers = () => {
    navigate(`/groups/${groupId}/members`);
  };

  const handleGoToSettings = () => {
    navigate(`/groups/${groupId}/settings`);
  };

  if (isLoading) {
    return (
      <S.PageContainer>
        <S.LoadingState>로딩 중...</S.LoadingState>
      </S.PageContainer>
    );
  }

  if (!groupData) {
    return (
      <S.PageContainer>
        <S.LoadingState>그룹을 찾을 수 없습니다.</S.LoadingState>
      </S.PageContainer>
    );
  }

  const group = groupData.data;

  return (
    <S.PageContainer>
      <S.Header>
        <S.TitleSection>
          <S.Title>{group.name}</S.Title>
          {group.description && <S.Description>{group.description}</S.Description>}
          <S.MemberCount>멤버 {group.memberCount}명</S.MemberCount>
        </S.TitleSection>
        <div>
          <Button title="멤버 관리" variant="secondary" onClick={handleGoToMembers} />
          {isOwner && <Button title="설정" variant="secondary" onClick={handleGoToSettings} />}
        </div>
      </S.Header>

      <S.Content>
        {isOwner && (
          <S.Section>
            <S.SectionTitle>초대</S.SectionTitle>
            <GroupInviteSection groupId={groupId || ''} />
          </S.Section>
        )}

        <S.Section>
          <S.SectionTitle>그룹 피드</S.SectionTitle>
          <S.Description>그룹 모멘트 피드는 추후 구현 예정입니다.</S.Description>
        </S.Section>
      </S.Content>
    </S.PageContainer>
  );
}
