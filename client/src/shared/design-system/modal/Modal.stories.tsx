import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Modal } from './Modal';
import { useModal } from '@/shared/hooks/useModal';
import { Button } from '@/shared/design-system/button/Button';
import { SimpleCard } from '../simpleCard';

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
    variant: {
      control: { type: 'radio' },
      options: ['default', 'memoji'],
    },
  },
  args: {
    position: 'center',
    size: 'medium',
    variant: 'default',
  },
};
export default meta;

type Story = StoryObj<typeof Modal>;

export const Default: Story = {
  args: {
    position: 'center',
    size: 'small',
    variant: 'default',
  },

  render: args => {
    const { handleOpen, handleClose, isOpen } = useModal();

    return (
      <>
        <Button title="기본 모달 열기" variant="secondary" onClick={handleOpen} />
        <Modal
          position={args.position}
          size={args.size}
          variant="default"
          isOpen={isOpen}
          onClose={handleClose}
        >
          <Modal.Header showCloseButton={true} />
          <Modal.Content>
            <p>기본 모달 컴포넌트입니다.</p>
            <SimpleCard height="small" content={<div>정말 멋진 모달이네요.</div>} />
            <Button title="공감하기" variant="primary" onClick={handleClose} />
          </Modal.Content>
        </Modal>
      </>
    );
  },
};

export const Memoji: Story = {
  render: args => {
    const { handleOpen, handleClose, isOpen } = useModal();

    return (
      <>
        <Button title="메모지 모달 열기" variant="secondary" onClick={handleOpen} />
        <Modal position={args.position} variant="memoji" isOpen={isOpen} onClose={handleClose}>
          <Modal.Header title="메모지 모달" showCloseButton={true} />
          <Modal.Content>
            <p>고정 크기의 메모지 스타일 모달입니다.</p>
          </Modal.Content>
        </Modal>
      </>
    );
  },
};
