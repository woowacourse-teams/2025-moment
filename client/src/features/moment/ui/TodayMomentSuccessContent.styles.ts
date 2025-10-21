import styled from '@emotion/styled';

export const TodayContentForm = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
`;

export const ActionContainer = styled.div`
  display: flex;
  gap: 16px;

  @media (max-width: 768px) {
    justify-content: center;
    flex-wrap: wrap;

    & > button {
      width: 80%;
      justify-content: center;
    }
  }
`;

export const ModalContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0px;
  font-size: 1.1rem;
  text-align: center;
`;

export const ModalActionContainer = styled.div`
  display: flex;
  gap: 12px;

  & > button {
    font-size: 1.1rem;
    padding: 10px 24px;
  }
`;
