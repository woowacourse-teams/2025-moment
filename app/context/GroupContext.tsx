import React, { createContext, useContext, useState, ReactNode } from "react";

interface GroupContextType {
  currentGroupId: string | null;
  setGroupId: (id: string | null) => void;
}

const GroupContext = createContext<GroupContextType | undefined>(undefined);

export function GroupProvider({ children }: { children: ReactNode }) {
  const [currentGroupId, setGroupId] = useState<string | null>(null);

  // TODO: Add persistence logic here if needed (AsyncStorage)

  return (
    <GroupContext.Provider value={{ currentGroupId, setGroupId }}>
      {children}
    </GroupContext.Provider>
  );
}

export function useGroup() {
  const context = useContext(GroupContext);
  if (context === undefined) {
    throw new Error("useGroup must be used within a GroupProvider");
  }
  return context;
}
