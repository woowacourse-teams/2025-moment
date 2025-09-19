import { theme } from '@/app/styles/theme';
import { EchoTypeKey } from '@/features/echo/type/echos';
import { Echo } from '@/features/echo/ui/Echo';
import { useReadNotifications } from '@/features/notification/hooks/useReadNotifications';
import { Button, Card, SimpleCard } from '@/shared/ui';
import Tag from '@/shared/ui/tag/Tag';
import { WriteTime } from '@/shared/ui/writeTime';
import { WriterInfo } from '@/widgets/writerInfo';
import { Heart, Send } from 'lucide-react';
import type { CommentWithNotifications } from '../types/commentsWithNotifications';
import * as S from './MyCommentsCard.styles';
import { useShowFullImage } from '@/shared/hooks/useShowFullImage';

export const MyCommentsCard = ({ myComment }: { myComment: CommentWithNotifications }) => {
  const { handleReadNotifications, isLoading: isReadingNotification } = useReadNotifications();
  const { fullImageSrc, handleImageClick, closeFullImage, ImageOverlayPortal } = useShowFullImage();

  const handleCommentOpen = () => {
    if (myComment.read || isReadingNotification) return;
    if (myComment.notificationId) {
      handleReadNotifications(myComment.notificationId);
    }
  };

  return (
    <>
      <Card width="medium" key={`card-${myComment.id}`} shadow={!myComment.read}>
        <Card.TitleContainer
          title={
            <S.TitleWrapper>
              <WriterInfo writer={myComment.moment.nickName} level={myComment.moment.level} />
              <WriteTime date={myComment.createdAt} />
            </S.TitleWrapper>
          }
          subtitle={''}
        />
        <Card.Content>
          <S.ContentContainer>
            <S.MomentContentWrapper>
              <S.MyMomentContent>{myComment.moment.content}</S.MyMomentContent>
              {myComment.moment.imageUrl && (
                <S.CommentImageContainer>
                  <S.CommentImage
                    src={myComment.moment.imageUrl}
                    alt="모멘트 이미지"
                    onClick={e => handleImageClick(myComment.moment.imageUrl!, e)}
                  />
                </S.CommentImageContainer>
              )}
            </S.MomentContentWrapper>
          </S.ContentContainer>
          <S.ContentContainer>
            <S.TitleContainer>
              <Send size={20} color={theme.colors['yellow-500']} />
              <p>보낸 코멘트</p>
            </S.TitleContainer>
            <SimpleCard
              height="small"
              content={
                <S.MyCommentsContentWrapper>
                  <p>{myComment.content}</p>
                  {myComment.imageUrl && (
                    <S.CommentImageContainer>
                      <S.CommentImage
                        src={myComment.imageUrl}
                        alt="코멘트 이미지"
                        onClick={e => handleImageClick(myComment.imageUrl!, e)}
                      />
                    </S.CommentImageContainer>
                  )}
                </S.MyCommentsContentWrapper>
              }
            />
          </S.ContentContainer>
          <S.ContentContainer>
            <S.TitleContainer>
              <Heart size={20} color={theme.colors['yellow-500']} />
              <p>받은 에코</p>
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
            {!myComment.read && <Button onClick={handleCommentOpen} title="확인" />}
          </S.ContentContainer>
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
