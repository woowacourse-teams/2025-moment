import { formatRelativeTime } from '@/shared/utils/formatRelativeTime';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { Clock } from 'lucide-react';

export const WriteTime = ({ date }: { date: string }) => {
  return (
    <TimeWrapper>
      <Clock size={16} />
      {formatRelativeTime(date)}
    </TimeWrapper>
  );
};

const TimeWrapper = styled.div`
  ${({ theme }) => css`
    color: ${theme.colors['gray-400']};
  `}
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 500;
`;
