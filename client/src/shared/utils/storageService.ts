export const getSessionStorage = (key: string) => {
  const value = sessionStorage.getItem(key);
  return value ? JSON.parse(value) : null;
};

export const setSessionStorage = (key: string, value: unknown) => {
  return sessionStorage.setItem(key, JSON.stringify(value));
};

export const removeSessionStorage = (key: string) => {
  return sessionStorage.removeItem(key);
};
