import styled from '@emotion/styled';

export const Footer = () => {
  return (
    <FooterWrapper>
      <div>
        문의: &nbsp;
        <InquiryLink href="https://open.kakao.com/o/shuZJySh" target="_blank">
          카카오톡 오픈 채팅
        </InquiryLink>
      </div>
      <div>Copyright © 우아한테크코스 모멘트 팀 All Rights Reserved.</div>
    </FooterWrapper>
  );
};

const FooterWrapper = styled.div`
  width: 100%;
  height: 100px;
  color: ${({ theme }) => theme.colors['gray-600']};
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
`;

const InquiryLink = styled.a`
  color: ${({ theme }) => theme.colors['gray-600']};
  text-decoration: underline;
  &:hover {
    color: ${({ theme }) => theme.colors['yellow-300_80']};
  }
`;
