import styled from '@emotion/styled';

export const EchoButton = ({
  title,
  onClick: handleClick,
  isSelected,
  isAlreadySent,
  isDisabled = false,
}: {
  title: string;
  onClick: () => void;
  isSelected: boolean;
  isAlreadySent?: boolean | undefined;
  isDisabled?: boolean;
}) => {
  return (
    <EchoButtonStyle
      type="button"
      onClick={handleClick}
      $isSelected={isSelected}
      $isDisabled={isDisabled}
      $isAlreadySent={isAlreadySent}
      disabled={isDisabled}
      aria-pressed={isSelected || !!isAlreadySent}
    >
      {title}
    </EchoButtonStyle>
  );
};

const EchoButtonStyle = styled.button<{
  $isSelected: boolean;
  $isDisabled: boolean;
  $isAlreadySent: boolean | undefined;
}>`
  border: 1px solid
    ${({ $isSelected, $isAlreadySent, theme }) => {
      if ($isSelected || $isAlreadySent) return theme.colors['yellow-500'];
      return theme.colors['gray-400'];
    }};
  color: ${({ $isSelected, $isAlreadySent, theme }) => {
    if ($isSelected || $isAlreadySent) return theme.colors['yellow-500'];
    return theme.colors['gray-400'];
  }};
  border-radius: 25px;
  padding: 4px 20px;
  font-size: 14px;
  font-weight: bold;
  cursor: ${({ $isDisabled }) => ($isDisabled ? 'not-allowed' : 'pointer')};
`;
