import { useLoginForm } from "../hooks/useLoginForm";
import * as S from "./LoginForm.styles";

export function LoginForm() {
  const {
    email,
    password,
    error,
    isLoading,
    setEmail,
    setPassword,
    handleSubmit,
  } = useLoginForm();

  return (
    <S.Form onSubmit={handleSubmit}>
      <S.Title>Moment Admin</S.Title>
      {error && <S.ErrorMessage>{error}</S.ErrorMessage>}
      <S.InputGroup>
        <S.Label htmlFor="email">Email</S.Label>
        <S.Input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="admin@gmail.com"
          required
        />
      </S.InputGroup>
      <S.InputGroup>
        <S.Label htmlFor="password">Password</S.Label>
        <S.Input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
        />
      </S.InputGroup>
      <S.SubmitButton type="submit" disabled={isLoading}>
        {isLoading ? "Logging in..." : "Login"}
      </S.SubmitButton>
    </S.Form>
  );
}
