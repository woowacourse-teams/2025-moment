import { Meta, StoryObj } from '@storybook/react-webpack5';
import { AlarmClock } from 'lucide-react';
import { Button } from '../button/Button';
import { TextArea } from '../textArea/TextArea';
import { Card } from './Card';

const meta: Meta<typeof Card> = {
  title: 'Shared/Card',
  component: Card,
  argTypes: {
    width: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'],
    },
  },
  args: {
    width: 'small',
    children: 'Hello',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: args => {
    return (
      <Card width={args.width}>
        <Card.TitleContainer title="Card Title" subtitle="Card Subtitle" Icon={AlarmClock} />
        <Card.Content>
          <TextArea placeholder="Enter your message" height="medium" />
        </Card.Content>
        <Card.Action position="space-between">
          <Button variant="primary" title="확인" />
          <Button variant="primary" title="취소" />
        </Card.Action>
      </Card>
    );
  },
};

export const SimpleCard: Story = {
  args: {
    width: 'medium',
  },
  render: args => {
    return (
      <Card width={args.width}>
        <Card.Content>
          <p>간단한 카드 내용입니다.</p>
        </Card.Content>
      </Card>
    );
  },
};

export const WithTitleOnly: Story = {
  args: {
    width: 'large',
  },
  render: args => {
    return (
      <Card width={args.width}>
        <Card.TitleContainer title="제목만 있는 카드" subtitle="부제목도 함께" />
        <Card.Content>
          <p>제목과 내용만 있는 심플한 카드입니다.</p>
        </Card.Content>
      </Card>
    );
  },
};
