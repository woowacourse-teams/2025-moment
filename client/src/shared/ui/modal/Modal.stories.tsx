import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Modal } from './Modal';
import { useModal } from '@/shared/hooks/useModal';
import styled from '@emotion/styled';
import { Send } from 'lucide-react';
import { SimpleCard } from '@/shared/ui';
import { theme } from '@/app/styles/theme';
import { Button } from '@/shared/ui/button/Button';

const meta: Meta<typeof Modal> = {
  title: 'Shared/Modal',
  component: Modal,
  argTypes: {
    position: {
      control: { type: 'radio' },
      options: ['center', 'bottom'],
    },
    size: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'],
    },
  },
  args: {
    position: 'center',
    size: 'medium',
  },
};
export default meta;

type Story = StoryObj<typeof Modal>;

export const Default: Story = {
  render: args => {
    const { handleOpen, handleClose, isOpen } = useModal();

    return (
      <>
        <Button title="모달 열기" variant="secondary" onClick={handleOpen} />
        <Modal position={args.position} size={args.size} isOpen={isOpen} onClose={handleClose}>
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            <p>공용 모달 컴포넌트를 만들어봤어요.</p>
            <TitleContainer>
              <Send size={20} color={theme.colors['yellow-500']} />
              <span>받은 공감</span>
            </TitleContainer>
            <SimpleCard height="small" content={<div>정말 멋진 모달이네요.</div>} />
            <Button title="공감하기" variant="primary" onClick={handleClose} />
          </Modal.Content>
        </Modal>
      </>
    );
  },
};

const TitleContainer = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
`;
