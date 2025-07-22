import styled from '@emotion/styled';

export const Category = ({ text }: { text: string }) => {
  return <CategoryStyle>{text}</CategoryStyle>;
};

const CategoryStyle = styled.span`
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid ${({ theme }) => theme.colors['blue-600']};
  color: ${({ theme }) => theme.colors['blue-600']};
  height: 20px;
  width: 120px;
  border-radius: 25px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: bold;
`;
