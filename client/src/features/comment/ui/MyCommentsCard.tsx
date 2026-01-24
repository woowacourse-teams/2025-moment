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

export const MyCommentsCard = ({ myComment }: { myComment: CommentItem }) => {
  const { handleCommentOpen, fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } =
    useMyCommentsCard(myComment);

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
                <WriteTime date={myComment.createdAt} />
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
