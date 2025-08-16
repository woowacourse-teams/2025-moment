import { ECHO_TYPE } from '../const/echoType';
import { EchoButton } from './EchoButton';

type EchoKey = keyof typeof ECHO_TYPE;

interface EchoButtonGroupProps {
  onToggle: (echoType: EchoKey) => void;
  isSelected: (echoType: EchoKey) => boolean;
}

export const EchoButtonGroup = ({ onToggle, isSelected }: EchoButtonGroupProps) => {
  return (
    <>
      {Object.entries(ECHO_TYPE).map(([key, title]) => (
        <EchoButton
          key={key}
          onClick={() => onToggle(key as EchoKey)}
          title={title}
          isSelected={isSelected(key as EchoKey)}
        />
      ))}
    </>
  );
};
