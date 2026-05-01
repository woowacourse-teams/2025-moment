import styled from '@emotion/styled';

export const Page = styled.div`
  min-height: 100vh;
  padding: 24px 24px 40px;
  max-width: 1440px;
  margin: 0 auto;

  ${({ theme }) => theme.breakpoints.mobile} {
    padding: 16px 16px 40px;
  }
`;

export const Header = styled.div`
  margin-bottom: 20px;
`;

export const Title = styled.h1`
  font-size: 1.75rem;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.white};
  margin-bottom: 8px;
`;

export const Subtitle = styled.p`
  font-size: 0.875rem;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const RunHint = styled.code`
  display: inline-block;
  margin-top: 4px;
  padding: 2px 8px;
  background-color: ${({ theme }) => theme.colors['slate-800']};
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
  border-radius: 4px;
  font-size: 0.8rem;
  color: ${({ theme }) => theme.colors['yellow-500']};
`;

export const StatGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;

  ${({ theme }) => theme.breakpoints.mobile} {
    grid-template-columns: repeat(2, 1fr);
  }
`;

export const StatCard = styled.div`
  background-color: ${({ theme }) => theme.colors['slate-800']};
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
  border-radius: 12px;
  padding: 16px 20px;
`;

export const StatLabel = styled.p`
  font-size: 0.8rem;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-bottom: 8px;
`;

export const StatValue = styled.p<{ $accent?: boolean }>`
  font-size: 1.5rem;
  font-weight: 700;
  color: ${({ theme, $accent }) => ($accent ? theme.colors['yellow-500'] : theme.colors.white)};
`;

export const StatUnit = styled.span`
  font-size: 1rem;
  font-weight: 500;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-left: 4px;
`;

export const ChartGrid = styled.div<{ $cols?: 2 | 3 }>`
  display: grid;
  grid-template-columns: ${({ $cols }) => ($cols === 3 ? 'repeat(3, 1fr)' : '1fr 1fr')};
  gap: 16px;
  margin-bottom: 16px;

  ${({ theme }) => theme.breakpoints.mobile} {
    grid-template-columns: 1fr;
  }
`;

export const ChartCard = styled.div`
  background-color: ${({ theme }) => theme.colors['slate-800']};
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
  border-radius: 12px;
  padding: 20px 20px 16px;
`;

export const ChartTitle = styled.h2`
  font-size: 0.8rem;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-200']};
  margin-bottom: 20px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
`;

export const ErrorBox = styled.div`
  background-color: ${({ theme }) => theme.colors['slate-800']};
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
  border-radius: 12px;
  padding: 48px 32px;
  text-align: center;
`;

export const ErrorTitle = styled.p`
  font-size: 1rem;
  color: ${({ theme }) => theme.colors.white};
  margin-bottom: 12px;
`;

export const ErrorDesc = styled.p`
  font-size: 0.875rem;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-bottom: 16px;
`;

export const LoadingBox = styled.div`
  text-align: center;
  padding: 80px 0;
  color: ${({ theme }) => theme.colors['gray-400']};
  font-size: 0.9rem;
`;
