import { SignupFormData } from '@/features/auth/types/signup';
import { Button } from '@/shared/design-system/button/Button';
import { Modal } from '@/shared/design-system/modal/Modal';
import { useEnterKeyHandler } from '@/shared/hooks/useEnterKeyHandler';
import { TermsContent } from '@/pages/terms/TermsContent';
import { useState } from 'react';
import * as S from './SignupStep3.styles';

interface SignupStep3Props {
  signupData: SignupFormData;
  handleClick: () => void;
  onEnter?: () => void;
}

export const SignupStep3 = ({ signupData, handleClick, onEnter }: SignupStep3Props) => {
  const [agreedToTerms, setAgreedToTerms] = useState(false);
  const [isTermsOpen, setIsTermsOpen] = useState(false);

  useEnterKeyHandler(agreedToTerms ? onEnter : undefined);

  return (
    <S.StepContainer>
      <S.InfoContainer>
        <S.InfoItem>
          <S.InfoLabel>이메일</S.InfoLabel>
          <S.InfoValue>{signupData.email}</S.InfoValue>
        </S.InfoItem>
        <S.InfoItem>
          <S.InfoLabel>닉네임</S.InfoLabel>
          <S.InfoValue>{signupData.nickname}</S.InfoValue>
        </S.InfoItem>
      </S.InfoContainer>

      <S.Description>
        <p>입력하신 정보가 맞는지 확인해 주세요.</p>
        <p>확인 후 회원가입 버튼을 눌러주세요.</p>
      </S.Description>

      <S.TermsContainer>
        <S.TermsCheckboxLabel>
          <S.TermsCheckbox
            type="checkbox"
            checked={agreedToTerms}
            onChange={e => setAgreedToTerms(e.target.checked)}
          />
          [필수] 이용약관에 동의합니다
        </S.TermsCheckboxLabel>
        <S.TermsLink type="button" onClick={() => setIsTermsOpen(true)}>
          이용약관 보기
        </S.TermsLink>
      </S.TermsContainer>

      <S.ButtonContainer>
        <Button
          title="회원가입"
          variant="primary"
          onClick={handleClick}
          disabled={!agreedToTerms}
        />
      </S.ButtonContainer>

      <Modal
        isOpen={isTermsOpen}
        position="center"
        size="medium"
        onClose={() => setIsTermsOpen(false)}
      >
        <Modal.Header title="이용약관" showCloseButton={true} />
        <Modal.Content>
          <S.TermsModalContent>
            <TermsContent />
          </S.TermsModalContent>
        </Modal.Content>
      </Modal>
    </S.StepContainer>
  );
};
