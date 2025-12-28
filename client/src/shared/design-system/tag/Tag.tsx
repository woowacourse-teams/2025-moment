import styled from '@emotion/styled';

export interface TagProps {
  tag: string;
  onClick?: () => void;
  selected?: boolean;
}

export const Tag = ({ tag, onClick, selected }: TagProps) => {
  return (
    <TagStyles
      type="button"
      onClick={onClick}
      $selected={selected ?? false}
      aria-pressed={selected ?? false}
      aria-label={`태그: ${tag}`}
    >
      {tag}
    </TagStyles>
  );
};

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
