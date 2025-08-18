import * as S from './EXPBar.styles';

interface EXPBar {
  progress: number;
}

export const EXPBar = ({ progress }: EXPBar) => {
  return (
    <S.EXPBarContainer>
      <S.EXPBar>
        <S.EXPBarProgress progress={progress}></S.EXPBarProgress>
      </S.EXPBar>
    </S.EXPBarContainer>
  );
};
