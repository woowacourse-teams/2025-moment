import { Meta, StoryObj } from '@storybook/react-webpack5';
import { TextArea } from './TextArea';

const meta: Meta<typeof TextArea> = {
  title: 'Shared/TextArea',
  component: TextArea,
  argTypes: {
    height: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'],
    },
    disabled: {
      control: { type: 'boolean' },
    },
  },
  args: {
    placeholder: 'Enter your message',
    height: 'small',
    disabled: false,
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Large: Story = {
  args: {
    height: 'large',
    placeholder: '긴 텍스트를 입력하세요',
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
    placeholder: '비활성화된 텍스트 영역',
  },
};
