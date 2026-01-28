import styled from "@emotion/styled";

export const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: radial-gradient(circle at 50% 50%, #1e1b4b 0%, #0f172a 100%);
  color: white;
  position: relative;
  overflow: hidden;
`;

export const BackgroundOverlay = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(
      circle at 15% 50%,
      rgba(99, 102, 241, 0.15) 0%,
      transparent 25%
    ),
    radial-gradient(
      circle at 85% 30%,
      rgba(139, 92, 246, 0.15) 0%,
      transparent 25%
    );
  pointer-events: none;
`;
