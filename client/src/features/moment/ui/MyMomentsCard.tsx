import { Modal } from '@/shared/design-system/modal/Modal';
import { ChevronLeft, ChevronRight, Mail, Siren } from 'lucide-react';
import { WriteTime } from '@/shared/ui/writeTime/WriteTime';
import { WriterInfo } from '@/widgets/writerInfo';
import * as S from './MyMomentsCard.styles';
import { theme } from '@/shared/styles/theme';
import { ComplaintModal } from '@/features/complaint/ui/ComplaintModal';
import { MyMomentsItem } from '../types/moments';
import { convertToWebp } from '@/shared/utils/convertToWebp';
import { useMyMomentsCard } from '../hook/useMyMomentsCard';

export const MyMomentsCard = ({ myMoment }: { myMoment: MyMomentsItem }) => {
  const {
    isOpen,
    isComplaintOpen,
    currentComment,
    fullImageSrc,
    sortedComments,
    navigation,
    handleModalClose,
    handleMomentClick,
    handleComplaintOpen,
    handleComplaintClose,
    handleComplaintSubmit,
    handleImageClick,
    closeFullImage,
    ImageOverlayPortal,
  } = useMyMomentsCard(myMoment);

  const hasComments = myMoment.comments ? myMoment.comments.length > 0 : false;

  return (
    <>
      <S.MyMomentsCard
        key={myMoment.momentId || myMoment.id}
        $hasComment={hasComments}
        onClick={hasComments ? handleMomentClick : undefined}
        $shadow={!myMoment.momentNotification.isRead}
        role={hasComments ? 'button' : undefined}
        tabIndex={hasComments ? 0 : undefined}
        onKeyDown={
          hasComments
            ? e => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleMomentClick();
                }
              }
            : undefined
        }
        aria-label={`${myMoment.content}에 달린 코멘트 확인하기`}
      >
        <S.MyMomentsTitleWrapper>
          <S.CommentCountWrapper aria-label={`달린 코멘트 수: ${sortedComments?.length}개`}>
            <Mail size={16} aria-hidden="true" />
            <span aria-hidden="true">{sortedComments?.length}</span>
          </S.CommentCountWrapper>
          <WriteTime date={myMoment.createdAt} />
        </S.MyMomentsTitleWrapper>
        <S.MyMomentsContent>{myMoment.content}</S.MyMomentsContent>
        <S.MyMomentsBottomWrapper>
          {myMoment.imageUrl ? (
            <S.MomentImageContainer>
              <S.MomentImage
                src={convertToWebp(myMoment.imageUrl)}
                alt="모멘트 이미지"
                onClick={e => handleImageClick(myMoment.imageUrl!, e)}
              />
            </S.MomentImageContainer>
          ) : (
            <div />
          )}
        </S.MyMomentsBottomWrapper>
      </S.MyMomentsCard>
      {isOpen && (
        <Modal
          isOpen={true}
          onClose={handleModalClose}
          variant="memoji"
          position="center"
          size="small"
          aria-labelledby="moment-modal-title"
          aria-role="dialog"
          aria-modal="true"
        >
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            {currentComment && (
              <>
                <S.MyMomentsModalContent key={currentComment.id}>
                  <S.CommentContentWrapper>
                    <S.MyMomentsModalHeader>
                      <S.WriterInfoWrapper>
                        <WriterInfo
                          writer={currentComment.memberNickname || currentComment.nickname}
                        />
                      </S.WriterInfoWrapper>
                      <S.TitleWrapper>
                        <WriteTime date={currentComment.createdAt} />
                        <S.ComplaintButton
                          onClick={handleComplaintOpen}
                          aria-label="코멘트 신고하기"
                        >
                          <Siren size={28} color={theme.colors['red-500']} />
                        </S.ComplaintButton>
                      </S.TitleWrapper>
                    </S.MyMomentsModalHeader>
                    <S.CommentContainer>
                      {navigation.hasPrevious && (
                        <S.CommentNavigationButton
                          onClick={navigation.goToPrevious}
                          position="left"
                          aria-label="이전 코멘트 보기"
                        >
                          <ChevronLeft size={16} />
                        </S.CommentNavigationButton>
                      )}

                      <S.CommentContent>
                        <div area-label={`${currentComment.content}`}>{currentComment.content}</div>
                        {currentComment.imageUrl && (
                          <S.CommentImageContainer>
                            <S.CommentImage
                              src={convertToWebp(currentComment.imageUrl)}
                              alt="코멘트 이미지"
                              onClick={e => handleImageClick(currentComment.imageUrl!, e)}
                            />
                          </S.CommentImageContainer>
                        )}
                      </S.CommentContent>

                      {navigation.hasNext && (
                        <S.CommentNavigationButton
                          onClick={navigation.goToNext}
                          position="right"
                          aria-label="다음 코멘트 보기"
                        >
                          <ChevronRight size={16} />
                        </S.CommentNavigationButton>
                      )}
                    </S.CommentContainer>
                    <S.CommentIndicator>
                      {navigation.currentIndex + 1} / {sortedComments?.length || 0}
                    </S.CommentIndicator>
                  </S.CommentContentWrapper>
                </S.MyMomentsModalContent>
              </>
            )}
          </Modal.Content>
        </Modal>
      )}
      {isComplaintOpen && (
        <ComplaintModal
          isOpen={isComplaintOpen}
          onClose={handleComplaintClose}
          targetId={currentComment.id}
          targetType="COMMENT"
          onSubmit={handleComplaintSubmit}
        />
      )}
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
