import React from 'react';
import { COMPLAINT_REASONS } from '../complaintReasons';

import * as S from './ComplaintReasonSelector.styles';
import { ComplaintReason } from '@/features/complaint/types/complaintType';

interface ComplaintReasonSelector {
  selectedReason: ComplaintReason | null;
  onReasonSelect: (reason: ComplaintReason) => void;
}

export const ComplaintReasonSelector: React.FC<ComplaintReasonSelector> = ({
  selectedReason,
  onReasonSelect,
}) => {
  return (
    <S.Container>
      <S.Title>신고 사유를 선택해주세요</S.Title>
      <S.ReasonList>
        {COMPLAINT_REASONS.map(reasonItem => (
          <S.ReasonItem
            key={reasonItem.value}
            isSelected={selectedReason === reasonItem.value}
            onClick={() => onReasonSelect(reasonItem.value)}
          >
            <S.ReasonHeader>
              <S.RadioButton isSelected={selectedReason === reasonItem.value} />
              <S.ReasonLabel>{reasonItem.label}</S.ReasonLabel>
            </S.ReasonHeader>
            <S.ReasonDescription>{reasonItem.description}</S.ReasonDescription>
          </S.ReasonItem>
        ))}
      </S.ReasonList>
    </S.Container>
  );
};
