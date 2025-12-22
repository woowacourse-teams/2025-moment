import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Input } from './Input';

const meta: Meta<typeof Input> = {
  title: 'Shared/Input',
  component: Input,
  argTypes: {
    type: {
      control: { type: 'radio' },
      options: ['text', 'password', 'email'],
    },
    disabled: {
      control: { type: 'boolean' },
    },
  },
  args: {
    placeholder: 'Enter your email',
    type: 'text',
    disabled: false,
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Password: Story = {
  args: {
    type: 'password',
    placeholder: '비밀번호를 입력하세요',
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
    placeholder: '비활성화된 입력',
  },
};
