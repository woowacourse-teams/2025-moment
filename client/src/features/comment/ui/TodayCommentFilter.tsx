import styled from '@emotion/styled';
import { FilterType } from '../types/comments';
import { Button } from '@/shared/design-system/button';

interface TodayCommentFilter {
  activeFilter: FilterType;
  onActiveFilterChange: (filter: FilterType) => void;
}

export const TodayCommentFilter = ({ activeFilter, onActiveFilterChange }: TodayCommentFilter) => {
  return (
    <FilterContainer>
      <Button
        variant={activeFilter === 'all' ? 'quinary' : 'quaternary'}
        onClick={() => onActiveFilterChange('all')}
        aria-label="전체 코멘트 보기"
      >
        전체
      </Button>
      <Button
        variant={activeFilter === 'unread' ? 'quinary' : 'quaternary'}
        onClick={() => onActiveFilterChange('unread')}
        aria-label="미확인 코멘트 보기"
      >
        미확인 알림
      </Button>
    </FilterContainer>
  );
};

const FilterContainer = styled.div`
  display: flex;
  justify-content: center;
  gap: 4px;
`;
