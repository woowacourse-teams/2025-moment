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
  width: 100%;
  display: flex;
  align-items: center;
  gap: 4px;
`;

const LevelIcon = styled.img`
  width: 20px;
  height: 20px;
`;

const WriterName = styled.span`
  font-size: 1rem;
`;
