type StorageType = 'session' | 'local';

const getStorage = (type: StorageType) => {
  if (typeof window === 'undefined') {
    return null;
  }
  return type === 'session' ? sessionStorage : localStorage;
};

const getItem = <T = string>(key: string, type: StorageType = 'session'): T | null => {
  const storage = getStorage(type);
  if (!storage) return null;

  try {
    const item = storage.getItem(key);
    if (item === null) return null;

    try {
      return JSON.parse(item) as T;
    } catch {
      return item as T;
    }
  } catch {
    return null;
  }
};

const setItem = <T>(key: string, value: T, type: StorageType = 'session'): void => {
  const storage = getStorage(type);
  if (!storage) return;

  try {
    const stringValue = typeof value === 'string' ? value : JSON.stringify(value);
    storage.setItem(key, stringValue);
  } catch (error) {
    console.error(`Failed to set item in ${type}Storage:`, error);
  }
};

const removeItem = (key: string, type: StorageType = 'session'): void => {
  const storage = getStorage(type);
  if (!storage) return;

  try {
    storage.removeItem(key);
  } catch (error) {
    console.error(`Failed to remove item from ${type}Storage:`, error);
  }
};

const removeItems = (keys: string[], type: StorageType = 'session'): void => {
  keys.forEach(key => removeItem(key, type));
};

const clear = (type: StorageType = 'session'): void => {
  const storage = getStorage(type);
  if (!storage) return;

  try {
    storage.clear();
  } catch (error) {
    console.error(`Failed to clear ${type}Storage:`, error);
  }
};

export const storageService = {
  session: {
    get: <T = string>(key: string) => getItem<T>(key, 'session'),
    set: <T>(key: string, value: T) => setItem(key, value, 'session'),
    remove: (key: string) => removeItem(key, 'session'),
    removeMultiple: (keys: string[]) => removeItems(keys, 'session'),
    clear: () => clear('session'),
  },
  local: {
    get: <T = string>(key: string) => getItem<T>(key, 'local'),
    set: <T>(key: string, value: T) => setItem(key, value, 'local'),
    remove: (key: string) => removeItem(key, 'local'),
    removeMultiple: (keys: string[]) => removeItems(keys, 'local'),
    clear: () => clear('local'),
  },
};
