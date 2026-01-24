import { TodayCommentForm } from '@/features/comment/ui/TodayCommentForm';
import { TitleContainer } from '@/shared/design-system/titleContainer/TitleContainer';
import * as S from '../todayMoment/index.styles';
import { TagList } from '@/shared/design-system/tag/TagList';
import { useState } from 'react';
import { useCommentableMomentsQuery } from '@/features/comment/api/useCommentableMomentsQuery';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { TAGS } from '@/features/moment/const/tags';
import { useDwell } from '@/shared/lib/ga/hooks/useDwell';

import { useParams } from 'react-router';

export default function TodayCommentPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const { data: isLoggedIn, isLoading: isLoggedInLoading } = useCheckIfLoggedInQuery();
  const [selectedTag, setSelectedTag] = useState<string | undefined>(undefined);
  const {
    data: momentData,
    isLoading,
    error,
    refetch,
  } = useCommentableMomentsQuery(groupId, { enabled: isLoggedIn === true }, selectedTag);

  useDwell({ item_type: 'comment', surface: 'composer' });

  const handleTagClick = (tag: string) => {
    const newSelectedTag = selectedTag === tag ? undefined : tag;
    setSelectedTag(newSelectedTag);
  };

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 코멘트"
        subtitle="특별한 모멘트를 공유받고 따뜻한 공감을 보내보세요"
      />
      <TagList
        tags={TAGS}
        onTagClick={handleTagClick}
        selectedTag={selectedTag ? [selectedTag] : undefined}
      />
      <TodayCommentForm
        momentData={momentData}
        isLoading={isLoading}
        isLoggedIn={isLoggedIn}
        isLoggedInLoading={isLoggedInLoading}
        error={error}
        refetch={refetch}
        groupId={groupId}
      />
    </S.TodayPageWrapper>
  );
}
