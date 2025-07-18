import { Meta, StoryObj } from '@storybook/react-webpack5';

const meta: Meta = {
  title: 'Example/Button',
  component: () => <button>Hello</button>,
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};
