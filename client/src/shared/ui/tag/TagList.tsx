import styled from '@emotion/styled';
import Tag from './Tag';

interface TagListProps {
  tags: string[];
  onTagClick: (tagName: string) => void;
  selectedTag?: string[];
}

export const TagList = ({ tags, onTagClick, selectedTag }: TagListProps) => {
  return (
    <TagListStyles role="group">
      {tags.map(tag => (
        <Tag
          key={tag}
          tag={tag}
          onClick={() => onTagClick(tag)}
          selected={selectedTag?.includes(tag)}
        />
      ))}
    </TagListStyles>
  );
};

const TagListStyles = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
`;
