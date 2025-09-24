import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import { CollectionHeader } from '@/pages/collection/CollectionHeader';
import * as S from '../index.styles';
import { TodayCommentFilter } from '@/features/comment/ui/TodayCommentFilter';
import { useState } from 'react';
import { FilterType } from '@/features/comment/types/comments';

export default function MyCommentCollectionPage() {
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');

  const handleActiveFilterChange = (filter: FilterType) => {
    setActiveFilter(filter);
  };

  return (
    <S.CollectionContainer>
      <CollectionHeader />
      <S.Description>내가 작성한 코멘트와 받은 에코를 확인해보세요</S.Description>
      <S.FilterWrapper>
        <TodayCommentFilter
          activeFilter={activeFilter}
          onActiveFilterChange={handleActiveFilterChange}
        />
      </S.FilterWrapper>

      <MyCommentsList filterType={activeFilter} />
    </S.CollectionContainer>
  );
}
