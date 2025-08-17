import { levelMap } from '@/app/layout/data/navItems';
import { useCommentableMomentsQuery } from '@/features/comment/api/useCommentableMomentsQuery';
import { Card, NotFound, SimpleCard } from '@/shared/ui';
import { CommonSkeletonCard } from '@/shared/ui/skeleton';
import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { AlertCircle, Clock, User } from 'lucide-react';
import * as S from './TodayCommentForm.styles';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';

export function TodayCommentForm() {
  const { data, isLoading, error } = useCommentableMomentsQuery();

  if (isLoading) {
    return <CommonSkeletonCard variant="comment" />;
  }

  if (error || !data) {
    return (
      <NotFound
        title="데이터를 불러올 수 없습니다"
        subtitle="잠시 후 다시 시도해주세요"
        icon={AlertCircle}
        size="large"
      />
    );
  }

  const momentData = data;

  const getLevelImage = (level: string) => {
    const levelKey = level.split('_')[0] as keyof typeof levelMap;
    return levelMap[levelKey] || levelMap.METEOR;
  };

  return (
    <Card width="medium">
      <Card.TitleContainer
        Icon={User}
        title={
          <S.TitleWrapper>
            <S.LevelImage src={getLevelImage(momentData.level)} alt={momentData.level} />
            <span>{momentData.nickname}</span>
            <S.TimeWrapper>
              <Clock size={14} />
              {formatRelativeTime(momentData.createdAt)}
            </S.TimeWrapper>
          </S.TitleWrapper>
        }
        subtitle=""
      />
      <SimpleCard height="small" content={momentData.content} />
      <TodayCommentWriteContent />
    </Card>
  );
}
