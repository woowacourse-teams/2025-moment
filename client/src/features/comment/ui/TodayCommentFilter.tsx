import { Button } from '@/shared/ui';
import styled from '@emotion/styled';
import { FilterType } from '../types/comments';

interface TodayCommentFilter {
  activeFilter: FilterType;
  onActiveFilterChange: (filter: FilterType) => void;
}

export const TodayCommentFilter = ({ activeFilter, onActiveFilterChange }: TodayCommentFilter) => {
  return (
    <FilterContainer>
      <Button
        title="전체"
        variant={activeFilter === 'all' ? 'quaternary' : 'primary'}
        onClick={() => onActiveFilterChange('all')}
      />
      <Button
        title="미확인 알림"
        variant={activeFilter === 'unread' ? 'quaternary' : 'primary'}
        onClick={() => onActiveFilterChange('unread')}
      />
    </FilterContainer>
  );
};

const FilterContainer = styled.div`
  display: flex;
  justify-content: center;
  gap: 4px;
`;
