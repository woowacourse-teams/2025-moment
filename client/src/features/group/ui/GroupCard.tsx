import { Group } from '../types/group';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import * as S from './GroupCard.styles';

interface GroupCardProps {
  group: Group;
  onClick: () => void;
}

export function GroupCard({ group, onClick }: GroupCardProps) {
  const { data: profile } = useProfileQuery();
  const isOwner = profile?.data.id === group.ownerId;

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <S.CardContainer onClick={onClick} role="button" tabIndex={0} aria-label={`${group.name} 그룹`}>
      <S.CardHeader>
        <S.GroupName>{group.name}</S.GroupName>
        {isOwner && <S.OwnerBadge>방장</S.OwnerBadge>}
      </S.CardHeader>

      {group.description && <S.Description>{group.description}</S.Description>}

      <S.CardFooter>
        <S.MemberCount>멤버 {group.memberCount}명</S.MemberCount>
        <S.CreatedDate>{formatDate(group.createdAt)}</S.CreatedDate>
      </S.CardFooter>
    </S.CardContainer>
  );
}
