import { theme } from '@/shared/styles/theme';
import { WriteTime } from '@/shared/ui/writeTime/WriteTime';
import { WriterInfo } from '@/widgets/writerInfo';
import { Send } from 'lucide-react';
import * as S from './MyCommentsCard.styles';
import type { CommentItem } from '../types/comments';
import { Card } from '@/shared/design-system/card';
import { SimpleCard } from '@/shared/design-system/simpleCard';
import { Button } from '@/shared/design-system/button';
import { convertToWebp } from '@/shared/utils/convertToWebp';
import { useMyCommentsCard } from '../hooks/useMyCommentsCard';
import { useDeleteCommentMutation } from '../api/useDeleteCommentMutation';
import { useCurrentGroup } from '@/features/group/hooks/useCurrentGroup';
import { Trash2, Heart } from 'lucide-react';
import { useMomentLikeMutation } from '@/features/moment/api/useMomentLikeMutation';
import { useCommentLikeMutation } from '../api/useCommentLikeMutation';

export const MyCommentsCard = ({ myComment }: { myComment: CommentItem }) => {
  const { currentGroupId } = useCurrentGroup();
  const deleteMutation = useDeleteCommentMutation(currentGroupId || '');
  const momentLikeMutation = useMomentLikeMutation(currentGroupId || '');
  const commentLikeMutation = useCommentLikeMutation(currentGroupId || '');

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (window.confirm('정말로 이 코멘트를 삭제하시겠습니까?')) {
      deleteMutation.mutate(myComment.id);
    }
  };

  const handleLikeMoment = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (myComment.moment) {
      momentLikeMutation.mutate(myComment.moment.id);
    }
  };

  const handleLikeComment = (e: React.MouseEvent) => {
    e.stopPropagation();
    commentLikeMutation.mutate(myComment.id);
  };
  const { handleCommentOpen, fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } =
    useMyCommentsCard(myComment, currentGroupId || '');

  return (
    <>
      <Card
        width="medium"
        key={`card-${myComment.id}`}
        shadow={!myComment.commentNotification.isRead}
      >
        {myComment.moment && (
          <Card.TitleContainer
            title={
              <S.TitleWrapper>
                <WriterInfo writer={myComment.moment.memberNickname || myComment.moment.nickName} />
                <S.ActionWrapper>
                  <WriteTime date={myComment.createdAt} />
                  <S.DeleteButton onClick={handleDelete} aria-label="코멘트 삭제">
                    <Trash2 size={24} color={theme.colors['red-500']} />
                  </S.DeleteButton>
                </S.ActionWrapper>
              </S.TitleWrapper>
            }
            subtitle={''}
          />
        )}
        <Card.Content>
          <S.ContentContainer>
            {myComment.moment ? (
              <S.MomentContentWrapper>
                <S.MyMomentContent aria-label={`모멘트 내용: ${myComment.moment.content}`}>
                  {myComment.moment.content}
                </S.MyMomentContent>
                <S.ActionWrapper>
                  <S.LikeButton onClick={handleLikeMoment} aria-label="모멘트 좋아요">
                    <Heart
                      size={20}
                      color={theme.colors['red-500']}
                      fill={myComment.moment.hasLiked ? theme.colors['red-500'] : 'none'}
                    />
                  </S.LikeButton>
                  <S.LikeCount>{myComment.moment.likeCount || 0}</S.LikeCount>
                </S.ActionWrapper>
                {myComment.moment?.imageUrl && (
                  <S.CommentImageContainer>
                    <S.CommentImage
                      src={convertToWebp(myComment.moment.imageUrl)}
                      alt="모멘트 이미지"
                      onClick={e => {
                        handleImageClick(myComment.moment!.imageUrl!, e);
                      }}
                    />
                  </S.CommentImageContainer>
                )}
              </S.MomentContentWrapper>
            ) : (
              <S.DeletedMomentText>삭제된 모멘트입니다</S.DeletedMomentText>
            )}
          </S.ContentContainer>
          <S.ContentContainer>
            <S.TitleContainer>
              <Send size={20} color={theme.colors['yellow-500']} />
              <S.SubTitle>보낸 코멘트</S.SubTitle>
              <S.ActionWrapper>
                <S.LikeButton onClick={handleLikeComment} aria-label="코멘트 좋아요">
                  <Heart
                    size={20}
                    color={theme.colors['red-500']}
                    fill={myComment.hasLiked ? theme.colors['red-500'] : 'none'}
                  />
                </S.LikeButton>
                <S.LikeCount>{myComment.likeCount || 0}</S.LikeCount>
              </S.ActionWrapper>
            </S.TitleContainer>
            <SimpleCard
              height="small"
              content={
                <S.MyCommentsContentWrapper>
                  <S.CommentContent area-label={`내가 쓴 코멘트 내용: ${myComment.content}`}>
                    {myComment.content}
                  </S.CommentContent>
                  {myComment.imageUrl && (
                    <S.CommentImageContainer>
                      <S.CommentImage
                        src={convertToWebp(myComment.imageUrl)}
                        alt="코멘트 이미지"
                        onClick={e => handleImageClick(myComment.imageUrl!, e)}
                      />
                    </S.CommentImageContainer>
                  )}
                </S.MyCommentsContentWrapper>
              }
            />
          </S.ContentContainer>
          {!myComment.commentNotification.isRead && (
            <Button onClick={handleCommentOpen} title="확인" />
          )}
        </Card.Content>
      </Card>
      {fullImageSrc && (
        <ImageOverlayPortal>
          <S.ImageOverlay onClick={closeFullImage}>
            <S.FullscreenImage src={fullImageSrc} alt="전체 이미지" />
          </S.ImageOverlay>
        </ImageOverlayPortal>
      )}
    </>
  );
};
