import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Card } from './Card';
import { CardTitleContainer } from '../cardTitleContainer/CardTitleContainer';
import { AlarmClock } from 'lucide-react';
import { CardContent } from '../cardContent/CardContent';
import { TextArea } from '../textArea/TextArea.styles';
import { CardAction } from '../cardAction/CardAction';
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
        <CardTitleContainer title="Card Title" subtitle="Card Subtitle" Icon={AlarmClock} />
        <CardContent>
          <TextArea placeholder="Enter your message" height="medium" />
        </CardContent>
        <CardAction position="space-between">
          <Button variant="primary" title="확인" />
          <Button variant="primary" title="취소" />
        </CardAction>
      </Card>
    );
  },
};
