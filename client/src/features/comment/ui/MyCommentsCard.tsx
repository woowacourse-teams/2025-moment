import { theme } from '@/app/styles/theme';
import { EchoTypeKey } from '@/features/echo/type/echos';
import { Echo } from '@/features/echo/ui/Echo';
import { useReadNotifications } from '@/features/notification/hooks/useReadNotifications';
import { Button, Card, SimpleCard } from '@/shared/ui';
import Tag from '@/shared/ui/tag/Tag';
import { WriteTime } from '@/shared/ui/writeTime';
import { WriterInfo } from '@/widgets/writerInfo';
import { Heart, Send } from 'lucide-react';
import * as S from './MyCommentsCard.styles';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';
import { changeToCloudfrontUrlFromS3 } from '@/shared/utils/changeToCloudfrontUrlFromS3';
import type { CommentItem } from '../types/comments';

export const MyCommentsCard = ({ myComment }: { myComment: CommentItem }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();

  const handleCommentOpen = () => {
    if (myComment.commentNotification.isRead || isReadingNotification) return;
    if (myComment.commentNotification.notificationIds) {
      handleReadNotifications(myComment.commentNotification.notificationIds[0]);
    }
  };

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
                <WriterInfo writer={myComment.moment.nickName} level={myComment.moment.level} />
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
                <S.MyMomentContent>{myComment.moment.content}</S.MyMomentContent>
                {myComment.moment?.imageUrl && (
                  <S.CommentImageContainer>
                    <S.CommentImage
                      src={changeToCloudfrontUrlFromS3(myComment.moment.imageUrl)}
                      alt="모멘트 이미지"
                      onClick={e => {
                        handleImageClick(
                          changeToCloudfrontUrlFromS3(myComment.moment!.imageUrl!),
                          e,
                        );
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
                  <S.CommentContent>{myComment.content}</S.CommentContent>
                  {myComment.imageUrl && (
                    <S.CommentImageContainer>
                      <S.CommentImage
                        src={changeToCloudfrontUrlFromS3(myComment.imageUrl)}
                        alt="코멘트 이미지"
                        onClick={e =>
                          handleImageClick(changeToCloudfrontUrlFromS3(myComment.imageUrl!), e)
                        }
                      />
                    </S.CommentImageContainer>
                  )}
                </S.MyCommentsContentWrapper>
              }
            />
          </S.ContentContainer>
          {myComment.moment && (
            <S.ContentContainer>
              <S.TitleContainer>
                <Heart size={20} color={theme.colors['yellow-500']} />
                <S.SubTitle>받은 에코</S.SubTitle>
              </S.TitleContainer>
              <S.EchoContainer>
                {myComment.echos && myComment.echos.length > 0 ? (
                  myComment.echos.map(echo => (
                    <Echo key={echo.id} echo={echo.echoType as EchoTypeKey} />
                  ))
                ) : (
                  <S.NoEchoContent>아직 받은 에코가 없습니다.</S.NoEchoContent>
                )}
              </S.EchoContainer>
              <S.MyCommentsTagWrapper>
                {myComment.moment.tagNames.map((tag: string) => (
                  <Tag key={tag} tag={tag} />
                ))}
              </S.MyCommentsTagWrapper>
            </S.ContentContainer>
          )}
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
