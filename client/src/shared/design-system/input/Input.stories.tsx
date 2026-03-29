import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Input } from './Input';

const meta: Meta<typeof Input> = {
  title: 'Design System/Input',
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

export const AllTypes: Story = {
  render: () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 24, background: '#1E293B', width: 320 }}>
      <Input placeholder="텍스트 입력" type="text" />
      <Input placeholder="이메일 입력" type="email" />
      <Input placeholder="비밀번호 입력" type="password" />
    </div>
  ),
};

export const Disabled: Story = {
  args: {
    disabled: true,
    placeholder: '비활성화된 입력',
  },
};
