import { Meta, StoryObj } from '@storybook/react-webpack5';
import { useState } from 'react';
import { Tag } from './Tag';

const meta: Meta<typeof Tag> = {
  title: 'Shared/Tag',
  component: Tag,
  argTypes: {
    selected: {
      control: { type: 'boolean' },
    },
  },
  args: {
    tag: '태그',
    selected: false,
  },
};

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Selectable: Story = {
  render: args => {
    const [selected, setSelected] = useState(false);
    return <Tag {...args} selected={selected} onClick={() => setSelected(!selected)} />;
  },
};

export default meta;
