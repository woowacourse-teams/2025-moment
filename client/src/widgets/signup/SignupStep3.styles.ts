import styled from '@emotion/styled';

export const StepContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  margin-top: 24px;
  text-align: center;
`;

export const SuccessIcon = styled.div`
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: ${({ theme }) => theme.success.default};
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
`;

export const Title = styled.h2`
  font-size: 24px;
  font-weight: 700;
  color: ${({ theme }) => theme.text.primary};
  margin: 0;
`;

export const Description = styled.p`
  font-size: 16px;
  color: ${({ theme }) => theme.text.secondary};
  margin: 0;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;
