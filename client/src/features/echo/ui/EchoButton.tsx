import styled from '@emotion/styled';

interface EchoButton {
  title: string;
  onClick: () => void;
  isSelected: boolean;
}

export const EchoButton = ({ title, onClick: handleClick, isSelected }: EchoButton) => {
  return (
    <EchoButtonStyle onClick={handleClick} $isSelected={isSelected} aria-pressed={isSelected}>
      {title}
    </EchoButtonStyle>
  );
};

const EchoButtonStyle = styled.button<{
  $isSelected: boolean;
}>`
  border: 1px solid
    ${({ $isSelected, theme }) => {
      if ($isSelected) return theme.colors['yellow-500'];
      return theme.colors['gray-400'];
    }};
  color: ${({ $isSelected, theme }) => {
    if ($isSelected) return theme.colors['yellow-500'];
    return theme.colors['gray-400'];
  }};
  border-radius: 25px;
  padding: 4px 16px;
  font-size: 1rem;
  cursor: pointer;
`;
