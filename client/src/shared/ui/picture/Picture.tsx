import { ImgHTMLAttributes } from 'react';

interface PictureProps extends Omit<ImgHTMLAttributes<HTMLImageElement>, 'src'> {
    webpSrc: string;
    fallbackSrc: string;
    alt: string;
}

export const Picture = ({ webpSrc, fallbackSrc, alt, ...props }: PictureProps) => {
    return (
        <picture>
            <source srcSet={webpSrc} type="image/webp" />
            <img src={fallbackSrc} alt={alt} {...props} />
        </picture>
    );
};
