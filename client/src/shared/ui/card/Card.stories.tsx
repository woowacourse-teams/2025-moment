import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Card } from './Card';
import { CardTitleContainer } from './CardTitleContainer';
import { AlarmClock } from 'lucide-react';
import { CardContent } from './CardContent';
import { TextArea } from '../textArea/TextArea';
import { CardAction } from './CardAction';
import { Button } from '../button/Button';

const meta: Meta = {
  title: 'Example/Card',
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
