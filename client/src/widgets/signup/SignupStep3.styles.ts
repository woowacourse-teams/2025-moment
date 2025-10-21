import styled from '@emotion/styled';

export const StepContainer = styled.fieldset`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  margin-top: 24px;
  text-align: center;
  border: none;
  padding: 0;
  margin: 0;
`;

export const Title = styled.h2`
  font-size: 24px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.white};
  margin: 0;
`;

export const InfoContainer = styled.dl`
  width: 100%;
  max-width: 320px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 16px 0;
`;

export const InfoItem = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  background-color: ${({ theme }) => theme.colors['slate-800_60']};
`;

export const InfoLabel = styled.dt`
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['yellow-500']};
  text-align: left;
`;

export const InfoValue = styled.dd`
  font-size: 16px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors.white};
  text-align: left;
  margin: 0;
`;

export const Description = styled.p`
  font-size: 16px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const ButtonContainer = styled.div`
  width: 100%;
  max-width: 320px;
  margin-top: 16px;
`;
