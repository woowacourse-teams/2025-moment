import styled from '@emotion/styled';

interface WriterInfo {
  writer: string;
}

export const WriterInfo = ({ writer }: WriterInfo) => {
  return (
    <CommenterInfoContainer>
      <WriterName>{writer}</WriterName>
    </CommenterInfoContainer>
  );
};

const CommenterInfoContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

const WriterName = styled.span`
  font-size: 1.4rem;

  @media (max-width: 768px) {
    font-size: 1.2rem;
  }
`;
