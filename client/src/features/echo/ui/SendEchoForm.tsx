import * as S from './SendEchoForm.styles';
import { Heart } from 'lucide-react';
import { theme } from '@/app/styles/theme';
import { ECHO_TYPE } from '../const/echoType';
import { EchoButton } from './EchoButton';
import { SendEchoButton } from './SendEchoButton';
import type { EchoTypeKey } from '../type/echos';
import type { Comment } from '@/features/moment/types/moments';
import { Echo } from './Echo';
import { useEchoSelection } from '../hooks/useEchoSelection';
import { useEchoMutation } from '../hooks/useEchoMutation';

interface SendEchoForm {
  currentComment: Comment;
}

export const SendEchoForm = ({ currentComment }: SendEchoForm) => {
  const { selectedEchos, toggleEcho, clearSelection, isSelected, hasSelection } =
    useEchoSelection();

  const { mutateAsync: sendEchos } = useEchoMutation();

  const handleSendEchos = () => {
    sendEchos({ echoTypes: Array.from(selectedEchos), commentId: currentComment.id });
    clearSelection();
  };

  const echoType = currentComment?.echos.map(echo => echo.echoType);
  const hasAnyEcho = echoType && echoType.length > 0;

  return (
    <S.EchoContainer>
      <S.TitleContainer>
        <Heart size={16} color={theme.colors['yellow-500']} />
        <span>{hasAnyEcho ? '보낸 에코' : '에코 보내기'}</span>
      </S.TitleContainer>

      {hasAnyEcho ? (
        <S.EchoButtonContainer>
          {currentComment.echos.map(echo => (
            <Echo key={echo.id} echo={echo.echoType as EchoTypeKey} />
          ))}
        </S.EchoButtonContainer>
      ) : (
        <>
          <S.EchoButtonContainer>
            {Object.entries(ECHO_TYPE).map(([key, title]) => (
              <EchoButton
                key={key}
                title={title}
                onClick={() => toggleEcho(key as EchoTypeKey)}
                isSelected={isSelected(key as EchoTypeKey)}
              />
            ))}
          </S.EchoButtonContainer>
          <SendEchoButton isDisabled={!hasSelection} onClick={handleSendEchos} />
        </>
      )}
    </S.EchoContainer>
  );
};
