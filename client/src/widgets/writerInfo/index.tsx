import { LEVEL_MAP } from '@/app/layout/data/navItems';
import styled from '@emotion/styled';

interface WriterInfo {
  writer: string;
  level: string;
}

export const WriterInfo = ({ writer, level }: WriterInfo) => {
  return (
    <CommenterInfoContainer>
      <LevelIcon src={LEVEL_MAP[level as keyof typeof LEVEL_MAP]} alt="level" />
      <WriterName>{writer}</WriterName>
    </CommenterInfoContainer>
  );
};

const CommenterInfoContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

const LevelIcon = styled.img`
  width: 28px;
  height: 28px;

  @media (max-width: 768px) {
    width: 20px;
    height: 20px;
  }
`;

const WriterName = styled.span`
  font-size: 1.4rem;

  @media (max-width: 768px) {
    font-size: 1.2rem;
  }
`;
