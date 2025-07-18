import { useCallback, useMemo } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router';

export function useFunnel<T extends readonly string[]>(steps: T) {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();

  const stepQuery = searchParams.get('step');
  const stepsArray = Array.from(steps);
  const step = stepsArray.includes(stepQuery || '') ? (stepQuery as T[number]) : steps[0];

  const setStep = useCallback(
    (nextStep: T[number]) => {
      const newSearchParams = new URLSearchParams(searchParams);
      newSearchParams.set('step', nextStep);
      navigate(`${location.pathname}?${newSearchParams.toString()}`, { replace: true });
    },
    [navigate, location.pathname, searchParams],
  );

  const Funnel = ({ children }: { children: React.ReactNode }) => <>{children}</>;

  const Step = useCallback(
    ({ name, children }: { name: T[number]; children: React.ReactNode }) => {
      return name === step ? <>{children}</> : null;
    },
    [step],
  );

  const currentStepIndex = useMemo(() => steps.indexOf(step), [step, steps]);

  const beforeStep = useMemo(() => {
    return currentStepIndex > 0 ? (steps[currentStepIndex - 1] as T[number]) : null;
  }, [currentStepIndex, steps]);

  const nextStep = useMemo(() => {
    return currentStepIndex < steps.length - 1 ? (steps[currentStepIndex + 1] as T[number]) : null;
  }, [currentStepIndex, steps]);

  return useMemo(
    () => ({
      Funnel,
      Step,
      useStep: () => ({ step, setStep }),
      beforeStep,
      nextStep,
    }),
    [Funnel, Step, step, setStep, beforeStep, nextStep],
  );
}
