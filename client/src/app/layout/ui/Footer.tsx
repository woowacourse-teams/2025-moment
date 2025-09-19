import * as S from './Footer.styles';

export const Footer = () => {
  return (
    <S.FooterWrapper>
      <div>
        문의: &nbsp;
        <S.InquiryLink href="http://pf.kakao.com/_txihUn" target="_blank">
          카카오톡 오픈 채팅
        </S.InquiryLink>
      </div>
      <div>Copyright © 우아한테크코스 모멘트 팀 All Rights Reserved.</div>
    </S.FooterWrapper>
  );
};
