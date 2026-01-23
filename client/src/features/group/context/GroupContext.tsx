import { Group } from '../types/group';
import { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface GroupContextType {
  currentGroup: Group | null;
  setCurrentGroup: (group: Group | null) => void;
  clearCurrentGroup: () => void;
}

const GroupContext = createContext<GroupContextType | undefined>(undefined);

const STORAGE_KEY = 'moment_current_group';

export function GroupProvider({ children }: { children: ReactNode }) {
  const [currentGroup, setCurrentGroupState] = useState<Group | null>(() => {
    // Initialize from localStorage
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  });

  const setCurrentGroup = (group: Group | null) => {
    setCurrentGroupState(group);
    if (group) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(group));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  const clearCurrentGroup = () => {
    setCurrentGroupState(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  useEffect(() => {
    // Sync with localStorage changes from other tabs
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === STORAGE_KEY) {
        const newValue = e.newValue ? JSON.parse(e.newValue) : null;
        setCurrentGroupState(newValue);
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  return (
    <GroupContext.Provider value={{ currentGroup, setCurrentGroup, clearCurrentGroup }}>
      {children}
    </GroupContext.Provider>
  );
}

export function useGroupContext() {
  const context = useContext(GroupContext);
  if (context === undefined) {
    throw new Error('useGroupContext must be used within a GroupProvider');
  }
  return context;
}
