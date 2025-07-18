import { useCallback, useMemo } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router';
import { STEPS } from '../types/step';
import type { Step } from '../types/step';

export function useFunnel<T extends readonly string[]>(steps: T) {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();

  const stepQuery = searchParams.get('step');
  const stepsArray = Array.from(steps);
  const step = stepsArray.indexOf(stepQuery || '') !== -1 ? (stepQuery as T[number]) : steps[0];

  const setStep = useCallback(
    (nextStep: T[number]) => {
      const newSearchParams = new URLSearchParams(searchParams);
      newSearchParams.set('step', nextStep);
      navigate(`${location.pathname}?${newSearchParams.toString()}`, { replace: true });
    },
    [navigate, location.pathname, searchParams],
  );

  const Funnel = useCallback(({ children }: { children: React.ReactNode }) => <>{children}</>, []);

  const Step = useCallback(
    ({ name, children }: { name: T[number]; children: React.ReactNode }) => {
      return name === step ? <>{children}</> : null;
    },
    [step],
  );

  const currentStepIndex = useMemo(() => STEPS.indexOf(step as Step), [step]);

  const beforeStep = useMemo(() => {
    return currentStepIndex > 0 ? (STEPS[currentStepIndex - 1] as Step) : null;
  }, [currentStepIndex]);

  const nextStep = useMemo(() => {
    return currentStepIndex < STEPS.length - 1 ? (STEPS[currentStepIndex + 1] as Step) : null;
  }, [currentStepIndex]);


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
