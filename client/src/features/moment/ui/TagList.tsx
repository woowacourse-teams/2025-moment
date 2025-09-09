import styled from '@emotion/styled';
import Tag from './Tag';

interface TagListProps {
  onTagClick: (tagName: string) => void;
  selectedTag?: string[];
}

export const TagList = ({ onTagClick, selectedTag }: TagListProps) => {
  const tags = ['일상/생각', '인간관계', '일/성장', '건강/운동', '취미/여가'];

  return (
    <TagListStyles>
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
  flex-wrap: wrap;
  gap: 8px;
`;
