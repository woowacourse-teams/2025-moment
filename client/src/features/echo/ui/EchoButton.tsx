import styled from '@emotion/styled';

export const EchoButton = ({
  title,
  onClick: handleClick,
  isSelected,
}: {
  title: string;
  onClick: () => void;
  isSelected: boolean;
}) => {
  return (
    <EchoButtonStyle onClick={handleClick} $isSelected={isSelected}>
      {title}
    </EchoButtonStyle>
  );
};

const EchoButtonStyle = styled.button<{ $isSelected: boolean }>`
  border: 1px solid
    ${({ $isSelected, theme }) =>
      $isSelected ? theme.colors['yellow-500'] : theme.colors['gray-400']};
  color: ${({ $isSelected, theme }) =>
    $isSelected ? theme.colors['yellow-500'] : theme.colors['gray-400']};
  border-radius: 25px;
  padding: 4px 20px;
  font-size: 14px;
  font-weight: bold;
`;
