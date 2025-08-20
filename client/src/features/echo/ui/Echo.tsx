import styled from '@emotion/styled';
import { EchoTypeKey } from '../type/echos';
import { echoMapping } from '../utils/echoMapping';

export const Echo = ({ echo }: { echo: EchoTypeKey }) => {
  return <EchoStyle>{echoMapping(echo)}</EchoStyle>;
};

const EchoStyle = styled.div`
  background-color: ${({ theme }) => theme.colors['yellow-300_50']};
  padding: 4px 16px;
  border-radius: 25px;
  font-size: 1rem;
  color: ${({ theme }) => theme.colors.white};

  @media (max-width: 768px) {
    padding: 2px 10px;
    font-size: 0.9rem;
  }
`;
