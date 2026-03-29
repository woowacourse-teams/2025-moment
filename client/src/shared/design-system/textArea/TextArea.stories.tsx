import { Meta, StoryObj } from '@storybook/react-webpack5';
import { TextArea } from './TextArea';
import { textAreaHeight } from './TextArea.styles';

const meta: Meta<typeof TextArea> = {
  title: 'Design System/TextArea',
  component: TextArea,
  argTypes: {
    height: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'] satisfies textAreaHeight[],
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

export const AllHeights: Story = {
  render: () => (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: 12,
        padding: 24,
        background: '#1E293B',
        width: 400,
      }}
    >
      {(['small', 'medium', 'large'] as textAreaHeight[]).map(height => (
        <TextArea key={height} height={height} placeholder={`height: ${height}`} />
      ))}
    </div>
  ),
};

export const Disabled: Story = {
  args: {
    disabled: true,
    placeholder: '비활성화된 텍스트 영역',
  },
};
