import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import styled from '@emotion/styled';

export default function MyCommentsPage() {
  return (
    <MyCommentsPageContainer>
      <TitleContainer title="보낸 코멘트" subtitle="내가 보낸 공감을 확인해보세요" />
      <MyCommentsList />
    </MyCommentsPageContainer>
  );
}

export const MyCommentsPageContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin: 20px;
`;
