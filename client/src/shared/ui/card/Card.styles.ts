import { CustomTheme } from "@/app/styles/theme";
import styled from "@emotion/styled";

export type cardWidth = "small" | "medium" | "large";

const CardStyles ={

    card: (theme: CustomTheme, width: cardWidth) => `
    display: flex;
    flex-direction: column;
    gap: 5px;
    width: ${theme.typography.cardWidth[width]};
    padding: 10px 20px;
    background-color: color-mix(in srgb, ${theme.colors.slate800} 60%, transparent);
    border-radius: 10px;
    border: 1px solid ${theme.colors.gray700};
    
    `
}

export const Card = styled.div<{width: cardWidth}>`
    ${({theme, width}) => CardStyles.card(theme, width)}
`;