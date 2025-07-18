import GlobalStyles from '@/app/styles/GlobalStyles';
import { theme } from '@/app/styles/theme';
import { Button } from '@/shared/ui/button/Button';
import { ThemeProvider } from '@emotion/react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

test('버튼 클릭 이벤트 동작', async () => {
  const onClick = jest.fn();
  render(
    <ThemeProvider theme={theme}>
      <GlobalStyles />
      <Button title="Click me" onClick={onClick} variant="primary" />
    </ThemeProvider>,
  );

  await userEvent.click(screen.getByText('Click me'));

  expect(onClick).toHaveBeenCalledTimes(1);
});
