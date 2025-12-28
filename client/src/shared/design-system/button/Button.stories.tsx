import { Meta, StoryObj } from '@storybook/react-webpack5';
import { User } from 'lucide-react';
import { Button } from './Button';

const meta: Meta<typeof Button> = {
  title: 'Shared/Button',
  component: Button,
  argTypes: {
    variant: {
      control: { type: 'radio' },
      options: ['primary', 'secondary', 'tertiary'],
    },
    disabled: {
      control: { type: 'boolean' },
    },
  },
  args: {
    variant: 'primary',
    title: 'Click me',
    disabled: false,
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const WithIcon: Story = {
  args: {
    Icon: User,
    title: '사용자',
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
    title: '비활성화',
  },
};
