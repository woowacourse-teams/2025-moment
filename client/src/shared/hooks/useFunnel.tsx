import { useCallback, useMemo } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router';

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

  return useMemo(
    () => ({
      Funnel,
      Step,
      useStep: () => ({ step, setStep }),
    }),
    [Funnel, Step, step, setStep],
  );
}
