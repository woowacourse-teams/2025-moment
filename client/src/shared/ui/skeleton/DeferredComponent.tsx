import { PropsWithChildren, useEffect, useState } from 'react';

interface DeferredComponentProps {
    delay?: number;
}

export const DeferredComponent = ({
    children,
    delay = 200,
}: PropsWithChildren<DeferredComponentProps>) => {
    const [isDeferred, setIsDeferred] = useState(false);

    useEffect(() => {
        const timeoutId = setTimeout(() => {
            setIsDeferred(true);
        }, delay);

        return () => clearTimeout(timeoutId);
    }, [delay]);

    if (!isDeferred) {
        return null;
    }

    return <>{children}</>;
};
