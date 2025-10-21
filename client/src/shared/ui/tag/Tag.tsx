import styled from '@emotion/styled';

const Tag = ({
  tag,
  onClick,
  selected,
}: {
  tag: string;
  onClick?: () => void;
  selected?: boolean;
}) => {
  return (
    <TagStyles
      type="button"
      onClick={onClick}
      $selected={selected ?? false}
      aria-pressed={selected ?? false}
    >
      {tag}
    </TagStyles>
  );
};

export default Tag;

const TagStyles = styled.button<{ $selected: boolean }>`
  padding: 2px 12px;
  border-radius: 50px;
  background-color: ${({ theme, $selected }) =>
    $selected ? theme.colors['yellow-300_10'] : theme.colors['gray-200_10']};
  border: 1px solid
    ${({ theme, $selected }) =>
    $selected ? theme.colors['yellow-500'] : theme.colors['gray-200_40']};
  color: ${({ theme, $selected }) =>
    $selected ? theme.colors['yellow-500'] : theme.colors['gray-200']};
`;
