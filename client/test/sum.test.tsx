import { Button } from '@/components/Button';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

test('버튼 클릭 이벤트 동작', async () => {
  const onClick = jest.fn();
  render(<Button title="Click me" onClick={onClick} />);

  await userEvent.click(screen.getByText('Click me'));

  expect(onClick).toHaveBeenCalledTimes(1);
});
