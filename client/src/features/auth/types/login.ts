export interface LoginFormData {
  email: string;
  password: string;
}

export interface LoginError {
  email?: string;
  password?: string;
}

export interface UseLoginReturn {
  formData: LoginFormData;
  error: LoginError;
  isLoading: boolean;
  handleInputChange: (
    field: keyof LoginFormData,
  ) => (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleSubmit: (e: React.FormEvent<HTMLFormElement>) => Promise<void>;
}
