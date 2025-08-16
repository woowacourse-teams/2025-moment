import { useState, useCallback } from 'react';
import { ECHO_TYPE } from '../const/echoType';

type EchoKey = keyof typeof ECHO_TYPE;

export const useEchoSelection = () => {
  const [selectedEchos, setSelectedEchos] = useState<Set<EchoKey>>(new Set());

  const toggleEcho = useCallback((echoType: EchoKey) => {
    setSelectedEchos(prev => {
      const newSet = new Set(prev);
      if (newSet.has(echoType)) {
        newSet.delete(echoType);
      } else {
        newSet.add(echoType);
      }
      return newSet;
    });
  }, []);

  const clearSelection = useCallback(() => {
    setSelectedEchos(new Set());
  }, []);

  const isSelected = useCallback(
    (echoType: EchoKey) => {
      return selectedEchos.has(echoType);
    },
    [selectedEchos],
  );

  return {
    selectedEchos,
    toggleEcho,
    clearSelection,
    isSelected,
    hasSelection: selectedEchos.size > 0,
  };
};
