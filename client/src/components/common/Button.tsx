interface ButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  title: string;
  onClick: () => void;
}

export const Button = ({ title, onClick, ...props }: ButtonProps) => {
  return (
    <button onClick={onClick} {...props}>
      {title}
    </button>
  );
};
