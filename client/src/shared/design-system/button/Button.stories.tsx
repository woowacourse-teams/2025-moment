import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Send, User } from 'lucide-react';
import { Button } from './Button';
import { ButtonVariant } from './Button.styles';

const meta: Meta<typeof Button> = {
  title: 'Design System/Button',
  component: Button,
  argTypes: {
    variant: {
      control: { type: 'select' },
      options: [
        'primary',
        'secondary',
        'tertiary',
        'quaternary',
        'quinary',
        'danger',
      ] satisfies ButtonVariant[],
    },
    disabled: {
      control: { type: 'boolean' },
    },
  },
  args: {
    variant: 'primary',
    children: 'Click me',
    disabled: false,
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const AllVariants: Story = {
  render: () => (
    <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', padding: 24, background: '#1E293B' }}>
      {(
        ['primary', 'secondary', 'tertiary', 'quaternary', 'quinary', 'danger'] as ButtonVariant[]
      ).map(variant => (
        <Button key={variant} variant={variant}>
          {variant}
        </Button>
      ))}
    </div>
  ),
};

export const WithLeftIcon: Story = {
  args: {
    leftIcon: <User size={16} />,
    children: '사용자',
  },
};

export const WithRightIcon: Story = {
  args: {
    rightIcon: <Send size={16} />,
    children: '전송',
    variant: 'tertiary',
  },
};

export const Disabled: Story = {
  render: () => (
    <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', padding: 24, background: '#1E293B' }}>
      {(['primary', 'secondary', 'tertiary', 'danger'] as ButtonVariant[]).map(variant => (
        <Button key={variant} variant={variant} disabled>
          {variant}
        </Button>
      ))}
    </div>
  ),
};
