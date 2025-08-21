"use strict";(self.webpackChunkclient=self.webpackChunkclient||[]).push([[300],{"./src/shared/ui/button/Button.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{$:()=>Button_Button});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const buttonStyles={primary:theme=>`\n    background-color: transparent;\n    color: #fff;\n    border: 1px solid ${theme.colors["slate-700"]};\n    border-radius: 50px;\n    padding: 10px 20px;\n    font-size: 16px;\n    font-weight: 600;\n    white-space: nowrap;\n\n    &:hover {\n        transform: scale(1.05);\n        transition: transform 0.3s ease;\n    }\n\n    @media (max-width: 768px) {\n        padding: 8px 16px;\n        font-size: 14px;\n    }\n    `,secondary:theme=>`\n    background-color: ${theme.colors["yellow-500"]};\n    color: black;\n    padding: 18px 30px;\n    border-radius: 50px;\n    font-size: 24px;\n    font-weight: bold;\n    transition: all 0.3s ease;\n\n    @media (max-width: 768px) {\n        padding: 16px 24px;\n        font-size: 20px;\n    }\n\n    @media (max-width: 480px) {\n        padding: 14px 20px;\n        font-size: 18px;\n    }\n\n    &:hover {\n        filter: brightness(1.1);\n        box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);\n        transform: translateY(-2px);\n    }\n    `,tertiary:theme=>`\n    background-color: ${theme.colors["yellow-500"]};\n    color: black;\n    padding: 10px 20px;\n    border-radius: 5px;\n    font-size: 16px;\n    font-weight: bold;\n    display: flex;\n    align-items: center;\n    gap: 10px;\n    transition: all 0.3s ease;\n\n    @media (max-width: 768px) {\n        padding: 16px 24px;\n        font-size: 14px;\n    }\n\n    @media (max-width: 480px) {\n        padding: 14px 20px;\n        font-size: 12px;\n    }\n\n    &:hover {\n        filter: brightness(1.1);\n        box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);\n        transform: translateY(-2px);\n    }\n    `},Button=emotion_styled_browser_esm.A.button`
  ${({theme,variant})=>buttonStyles[variant](theme)};
  ${({theme,externalVariant})=>externalVariant&&externalVariant(theme)};

  &:disabled {
    cursor: not-allowed;
    transform: none;
    color: ${({theme})=>theme.colors["slate-700"]};
    border: 1px solid ${({theme})=>theme.colors["slate-700"]};
  }
`,Button_Button=({variant="primary",externalVariant,title,onClick,disabled,Icon,...props})=>(0,jsx_runtime.jsxs)(Button,{variant,externalVariant,onClick,disabled,...props,children:[Icon&&(0,jsx_runtime.jsx)(Icon,{size:16}),title]});Button_Button.__docgenInfo={description:"",methods:[],displayName:"Button",props:{variant:{required:!1,tsType:{name:"union",raw:"'primary' | 'secondary' | 'tertiary'",elements:[{name:"literal",value:"'primary'"},{name:"literal",value:"'secondary'"},{name:"literal",value:"'tertiary'"}]},description:"",defaultValue:{value:"'primary'",computed:!1}},externalVariant:{required:!1,tsType:{name:"signature",type:"function",raw:"(theme: CustomTheme) => string",signature:{arguments:[{type:{name:"theme"},name:"theme"}],return:{name:"string"}}},description:""},title:{required:!0,tsType:{name:"string"},description:""},onClick:{required:!1,tsType:{name:"signature",type:"function",raw:"() => void",signature:{arguments:[],return:{name:"void"}}},description:""},disabled:{required:!1,tsType:{name:"boolean"},description:""},Icon:{required:!1,tsType:{name:"LucideIcon"},description:""}}}},"./src/shared/ui/card/Card.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{Z:()=>Card_Card});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const CardStyles_card=(theme,$width,$shadow)=>`\n    display: flex;\n    flex-direction: column;\n    gap: 15px;\n    width: ${theme.typography.cardWidth[$width]};\n    padding: 20px 30px;\n    background-color: ${theme.colors["slate-800_60"]};\n    border-radius: 10px;\n    border: 1px solid ${theme.colors["gray-700"]};\n    word-break: keep-all;\n    ${$shadow&&`\n      box-shadow: 0px 0px 15px ${theme.colors["yellow-300_80"]};\n      animation: shadowPulse 2s ease-in-out infinite;\n    `}\n\n    @keyframes shadowPulse {\n      0%, 100% {\n        box-shadow: 0px 0px 10px ${theme.colors["yellow-300_80"]};\n      }\n      50% {\n        box-shadow: 0px 0px 25px ${theme.colors["yellow-300_80"]};\n      }\n    }\n\n    @media (max-width: 768px) {\n      width: 90%;\n      padding: 10px 20px;\n    }\n    `,Card=emotion_styled_browser_esm.A.div`
  ${({theme,$width,$shadow})=>CardStyles_card(theme,$width,$shadow)}
`,CardAction=({children,position="center"})=>(0,jsx_runtime.jsx)(CardActionStyles,{position,children}),CardActionStyles=emotion_styled_browser_esm.A.section`
  display: flex;
  justify-content: ${({position})=>"center"===position?"center":"space-between"};
`;CardAction.__docgenInfo={description:"",methods:[],displayName:"CardAction",props:{children:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""},position:{required:!1,tsType:{name:"union",raw:"'center' | 'space-between'",elements:[{name:"literal",value:"'center'"},{name:"literal",value:"'space-between'"}]},description:"",defaultValue:{value:"'center'",computed:!1}}}};const CardContent=({children})=>(0,jsx_runtime.jsx)(CardContentStyles,{children}),CardContentStyles=emotion_styled_browser_esm.A.section`
  display: flex;
  flex-direction: column;
  text-align: center;
  gap: 5px;
  margin: 10px 0;
`;CardContent.__docgenInfo={description:"",methods:[],displayName:"CardContent",props:{children:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""}}};const CardTitleStyles_cardTitle=theme=>`\n    font-size: ${theme.typography.title.fontSize.small};\n    font-weight: ${theme.typography.fontWeight.large};\n    color: ${theme.colors.white};\n    `,CardSubtitleStyles_cardSubtitle=theme=>`\n    font-size: ${theme.typography.subTitle.fontSize.medium};\n    color: ${theme.colors["gray-200"]};\n    `,CardTitleWrapper=emotion_styled_browser_esm.A.div`
  display: flex;
  align-items: center;
  gap: 10px;
`,CardTitleContainer=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 5px;
`,CardTitle=emotion_styled_browser_esm.A.span`
  width: 100%;
  ${({theme})=>CardTitleStyles_cardTitle(theme)}
`,CardSubtitle=emotion_styled_browser_esm.A.span`
  width: 100%;
  ${({theme})=>CardSubtitleStyles_cardSubtitle(theme)}
`;var theme=__webpack_require__("./src/app/styles/theme.ts");const CardTitleContainer_CardTitleContainer=({Icon,title,subtitle})=>(0,jsx_runtime.jsxs)(CardTitleContainer,{children:[(0,jsx_runtime.jsxs)(CardTitleWrapper,{children:[Icon&&(0,jsx_runtime.jsx)(Icon,{size:32,color:theme.w.colors["yellow-500"]}),(0,jsx_runtime.jsx)(CardTitle,{children:title})]}),(0,jsx_runtime.jsx)(CardSubtitle,{children:subtitle})]});CardTitleContainer_CardTitleContainer.__docgenInfo={description:"",methods:[],displayName:"CardTitleContainer",props:{Icon:{required:!1,tsType:{name:"LucideIcon"},description:""},title:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""},subtitle:{required:!0,tsType:{name:"string"},description:""}}};const Card_Card=({children,width,shadow=!1})=>(0,jsx_runtime.jsx)(Card,{$width:width,$shadow:shadow,children});Card_Card.TitleContainer=CardTitleContainer_CardTitleContainer,Card_Card.Content=CardContent,Card_Card.Action=CardAction,Card_Card.__docgenInfo={description:"",methods:[{name:"TitleContainer",docblock:null,modifiers:["static"],params:[{name:"{ Icon, title, subtitle }: CardTitleContainerProps",optional:!1,type:{name:"CardTitleContainerProps",alias:"CardTitleContainerProps"}}],returns:null},{name:"Content",docblock:null,modifiers:["static"],params:[{name:"{ children }: CardContentProps",optional:!1,type:{name:"CardContentProps",alias:"CardContentProps"}}],returns:null},{name:"Action",docblock:null,modifiers:["static"],params:[{name:"{ children, position = 'center' }: CardActionProps",optional:!1,type:{name:"CardActionProps",alias:"CardActionProps"}}],returns:null}],displayName:"Card",props:{children:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""},width:{required:!0,tsType:{name:"union",raw:"'small' | 'medium' | 'large' | 'full'",elements:[{name:"literal",value:"'small'"},{name:"literal",value:"'medium'"},{name:"literal",value:"'large'"},{name:"literal",value:"'full'"}]},description:""},shadow:{required:!1,tsType:{name:"boolean"},description:"",defaultValue:{value:"false",computed:!1}}}}},"./src/shared/ui/index.ts":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{Zp:()=>Card.Z,Tn:()=>SimpleCard.T,y8:()=>Toast});__webpack_require__("./src/shared/ui/button/Button.tsx");var Card=__webpack_require__("./src/shared/ui/card/Card.tsx"),jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),react=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const ErrorContainer=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
  text-align: center;
  background: #0f172a;
  color: #ffffff;
`,ErrorTitle=emotion_styled_browser_esm.A.h1`
  font-size: 24px;
  margin-bottom: 16px;
  color: #ffffff;
`,ErrorMessage=emotion_styled_browser_esm.A.p`
  font-size: 16px;
  color: #93a1b7;
  margin-bottom: 32px;
  max-width: 400px;
`,ErrorButton=emotion_styled_browser_esm.A.button`
  background-color: #f1c40f;
  color: black;
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    filter: brightness(1.1);
    transform: translateY(-2px);
  }
`,ErrorFallback=({error,resetError})=>(0,jsx_runtime.jsxs)(ErrorContainer,{children:[(0,jsx_runtime.jsx)(ErrorTitle,{children:"문제가 발생했습니다"}),(0,jsx_runtime.jsx)(ErrorMessage,{children:"일시적인 문제가 발생했습니다. 다시 시도해 주세요."}),!1,(0,jsx_runtime.jsx)(ErrorButton,{onClick:resetError,children:"다시 시도"})]});ErrorFallback.__docgenInfo={description:"",methods:[],displayName:"ErrorFallback",props:{error:{required:!0,tsType:{name:"Error"},description:""},resetError:{required:!0,tsType:{name:"signature",type:"function",raw:"() => void",signature:{arguments:[],return:{name:"void"}}},description:""}}};var esm_exports=__webpack_require__("./node_modules/.pnpm/@sentry+core@9.43.0/node_modules/@sentry/core/build/esm/exports.js");class ErrorBoundary extends react.Component{constructor(props){super(props),this.state={hasError:!1}}static getDerivedStateFromError(error){return{hasError:!0,error}}componentDidCatch(error,errorInfo){console.error("ErrorBoundary caught an error:",error,errorInfo),esm_exports.Cp(error,{contexts:{react:{componentStack:errorInfo.componentStack}},tags:{errorBoundary:!0}})}resetErrorBoundary=()=>{this.setState({hasError:!1,error:void 0})};render(){const{hasError,error}=this.state,{children,fallback:FallbackComponent}=this.props;return hasError&&error?FallbackComponent?(0,jsx_runtime.jsx)(FallbackComponent,{error,resetError:this.resetErrorBoundary}):(0,jsx_runtime.jsx)(ErrorFallback,{error,resetError:this.resetErrorBoundary}):children}}ErrorBoundary.__docgenInfo={description:"",methods:[],displayName:"ErrorBoundary",props:{children:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""},fallback:{required:!1,tsType:{name:"ReactComponentType",raw:"React.ComponentType<ErrorFallbackProps>",elements:[{name:"ErrorFallbackProps"}]},description:""}}};var chunk_QMGIS6GS=__webpack_require__("./node_modules/.pnpm/react-router@7.6.3_react-dom@19.1.0_react@19.1.0__react@19.1.0/node_modules/react-router/dist/development/chunk-QMGIS6GS.mjs");emotion_styled_browser_esm.A.button`
  background-color: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
  }
`,emotion_styled_browser_esm.A.img`
  width: clamp(30px, 3vw, 50px);
  height: clamp(30px, 3vw, 50px);
  object-fit: contain;
  object-position: center;
  border-radius: 100%;
`,emotion_styled_browser_esm.A.span`
  font-size: 26px;
  color: white;
  font-weight: bold;
`;__webpack_require__("./src/shared/ui/notFound/NotFound.tsx");var SimpleCard=__webpack_require__("./src/shared/ui/simpleCard/SimpleCard.tsx"),circle_check_big=(__webpack_require__("./src/shared/ui/skeleton/CommonSkeletonCard.tsx"),__webpack_require__("./src/shared/ui/textArea/TextArea.tsx"),__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-check-big.js")),circle_x=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-x.js"),mail=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/mail.js"),x=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/x.js"),Toast_styles=__webpack_require__("./src/shared/ui/toast/Toast.styles.ts");const Toast=({message,variant,duration=3e3,routeType,onClose})=>{const[isExiting,setIsExiting]=(0,react.useState)(!1),navigate=(0,chunk_QMGIS6GS.Zp)();(0,react.useEffect)(()=>{if(duration>0){const timer=setTimeout(()=>{handleClose()},duration);return()=>clearTimeout(timer)}},[duration]);const handleClose=e=>{e?.stopPropagation(),setIsExiting(!0),setTimeout(()=>{onClose()},300)};return(0,jsx_runtime.jsxs)(Toast_styles.z2,{variant,isExiting,onClick:()=>{if("message"===variant&&routeType){navigate("moment"===routeType?"/collection/my-moment":"/collection/my-comment"),handleClose()}},$isClickable:"message"===variant&&!!routeType,children:[(0,jsx_runtime.jsx)(Toast_styles.We,{children:(()=>{switch(variant){case"success":return(0,jsx_runtime.jsx)(circle_check_big.A,{size:20});case"error":return(0,jsx_runtime.jsx)(circle_x.A,{size:20});case"message":return(0,jsx_runtime.jsx)(mail.A,{size:20});default:return null}})()}),(0,jsx_runtime.jsx)(Toast_styles.FH,{children:message}),(0,jsx_runtime.jsx)(Toast_styles.Jn,{onClick:e=>handleClose(e),"aria-label":"토스트 닫기",children:(0,jsx_runtime.jsx)(x.A,{size:16})})]})};Toast.__docgenInfo={description:"",methods:[],displayName:"Toast",props:{message:{required:!0,tsType:{name:"string"},description:""},variant:{required:!0,tsType:{name:"union",raw:"'success' | 'error' | 'message'",elements:[{name:"literal",value:"'success'"},{name:"literal",value:"'error'"},{name:"literal",value:"'message'"}]},description:""},duration:{required:!1,tsType:{name:"number"},description:"",defaultValue:{value:"3000",computed:!1}},routeType:{required:!1,tsType:{name:"union",raw:"'moment' | 'comment'",elements:[{name:"literal",value:"'moment'"},{name:"literal",value:"'comment'"}]},description:""},onClose:{required:!0,tsType:{name:"signature",type:"function",raw:"() => void",signature:{arguments:[],return:{name:"void"}}},description:""}}}},"./src/shared/ui/notFound/NotFound.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{M:()=>NotFound});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),eye=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/eye.js"),ui=__webpack_require__("./src/shared/ui/index.ts"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const NotFoundWrapper=emotion_styled_browser_esm.A.div`
  display: flex;
  width: 100%;
  flex-direction: column;
  height: ${({$size})=>"large"===$size?"55vh":"auto"};
  align-items: center;
  justify-content: center;
  gap: ${({$size})=>"large"===$size?"30px":"8px"};
`,NotFoundIconWrapper=emotion_styled_browser_esm.A.div`
  width: ${({$size})=>"large"===$size?"30px":"24px"};
  height: ${({$size})=>"large"===$size?"30px":"24px"};
  color: ${({theme})=>theme.colors["gray-400"]};
`,NotFoundContainer=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: ${({$size})=>"large"===$size?"10px":"8px"};
`,NotFoundTitle=emotion_styled_browser_esm.A.div`
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: ${({$size})=>"large"===$size?"28px":"14px"};
  font-weight: ${({$size})=>"large"===$size?"700":"400"};
  color: ${({theme})=>theme.colors["gray-400"]};
`,NotFoundSubtitle=emotion_styled_browser_esm.A.div`
  font-weight: ${({$size})=>"large"===$size?"600":"400"};
  font-size: ${({$size})=>"large"===$size?"16px":"14px"};
  color: ${({theme})=>theme.colors["gray-400"]};
`,NotFound=({title,subtitle,icon:IconComponent=eye.A,iconSize=24,size="large",withCard=!1})=>{const content=(0,jsx_runtime.jsxs)(NotFoundWrapper,{$size:size,children:[(0,jsx_runtime.jsx)(NotFoundIconWrapper,{$size:size,children:(0,jsx_runtime.jsx)(IconComponent,{size:iconSize})}),(0,jsx_runtime.jsxs)(NotFoundContainer,{$size:size,children:[(0,jsx_runtime.jsx)(NotFoundTitle,{$size:size,children:title}),(0,jsx_runtime.jsx)(NotFoundSubtitle,{$size:size,children:subtitle})]})]});return withCard?(0,jsx_runtime.jsx)(ui.Zp,{width:"medium",children:content}):content};NotFound.__docgenInfo={description:"",methods:[],displayName:"NotFound",props:{title:{required:!0,tsType:{name:"string"},description:""},subtitle:{required:!0,tsType:{name:"string"},description:""},icon:{required:!1,tsType:{name:"LucideIcon"},description:"",defaultValue:{value:"Eye",computed:!0}},iconSize:{required:!1,tsType:{name:"number"},description:"",defaultValue:{value:"24",computed:!1}},size:{required:!1,tsType:{name:"union",raw:"'small' | 'large'",elements:[{name:"literal",value:"'small'"},{name:"literal",value:"'large'"}]},description:"",defaultValue:{value:"'large'",computed:!1}},withCard:{required:!1,tsType:{name:"boolean"},description:"",defaultValue:{value:"false",computed:!1}}}}},"./src/shared/ui/simpleCard/SimpleCard.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{T:()=>SimpleCard_SimpleCard});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const SimpleCardStyles_simpleCard=(theme,$height,$backgroundColor)=>`\n    display: flex;\n    width: 100%;\n    padding: 10px 16px;\n    background-color: ${$backgroundColor?theme.colors[$backgroundColor]:theme.colors["gray-600_20"]};\n    border-radius: 5px;\n    height: ${theme.typography.textAreaHeight[$height]};\n    color: ${theme.colors["gray-400"]};\n    border: 1px solid ${theme.colors["gray-700"]};\n    `,SimpleCard=emotion_styled_browser_esm.A.div`
  ${({theme,$height,$backgroundColor})=>SimpleCardStyles_simpleCard(theme,$height,$backgroundColor)}
`,SimpleCard_SimpleCard=({height,content,backgroundColor})=>(0,jsx_runtime.jsx)(SimpleCard,{$height:height,$backgroundColor:backgroundColor,children:content});SimpleCard_SimpleCard.__docgenInfo={description:"",methods:[],displayName:"SimpleCard",props:{height:{required:!0,tsType:{name:"union",raw:"'small' | 'medium' | 'large'",elements:[{name:"literal",value:"'small'"},{name:"literal",value:"'medium'"},{name:"literal",value:"'large'"}]},description:""},content:{required:!0,tsType:{name:"ReactReactNode",raw:"React.ReactNode"},description:""},backgroundColor:{required:!1,tsType:{name:"unknown"},description:""}}}},"./src/shared/ui/skeleton/CommonSkeletonCard.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{X:()=>CommonSkeletonCard});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),theme=(__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js"),__webpack_require__("./src/app/styles/theme.ts")),emotion_react_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const shimmer=emotion_react_browser_esm.i7`
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: calc(200px + 100%) 0;
  }
`,SkeletonContainer=emotion_styled_browser_esm.A.div`
  width: ${({width})=>"number"==typeof width?`${width}px`:width};
  height: ${({height})=>"number"==typeof height?`${height}px`:height};
  border-radius: ${({borderRadius})=>"number"==typeof borderRadius?`${borderRadius}px`:borderRadius};
  background: linear-gradient(
    90deg,
    ${theme.w.colors["slate-800_60"]} 25%,
    ${theme.w.colors["gray-700"]} 50%,
    ${theme.w.colors["slate-800_60"]} 75%
  );
  background-size: 200px 100%;
  animation: ${shimmer} 4s ease-in-out infinite;
`,SkeletonTextContainer=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: ${({gap})=>gap};
`,Skeleton=(emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`,({width="100%",height="1rem",borderRadius="4px",className})=>(0,jsx_runtime.jsx)(SkeletonContainer,{width,height,borderRadius,className})),SkeletonText=({lines=1,lineHeight="1rem",gap="0.5rem"})=>(0,jsx_runtime.jsx)(SkeletonTextContainer,{gap,children:Array.from({length:lines}).map((_,index)=>(0,jsx_runtime.jsx)(Skeleton,{height:lineHeight,width:index===lines-1?"75%":"100%"},index))});Skeleton.__docgenInfo={description:"",methods:[],displayName:"Skeleton",props:{width:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'100%'",computed:!1}},height:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'1rem'",computed:!1}},borderRadius:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'4px'",computed:!1}},className:{required:!1,tsType:{name:"string"},description:""}}},SkeletonText.__docgenInfo={description:"",methods:[],displayName:"SkeletonText",props:{lines:{required:!1,tsType:{name:"number"},description:"",defaultValue:{value:"1",computed:!1}},lineHeight:{required:!1,tsType:{name:"string"},description:"",defaultValue:{value:"'1rem'",computed:!1}},gap:{required:!1,tsType:{name:"string"},description:"",defaultValue:{value:"'0.5rem'",computed:!1}}}};const SkeletonCard=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: ${({variant})=>"moment"===variant?"100%":theme.w.typography.cardWidth.medium};
  height: ${({variant})=>"moment"===variant?"350px":"auto"};
  padding: 20px 30px;
  background-color: ${theme.w.colors["slate-800_60"]};
  border-radius: 10px;
  border: 1px solid ${theme.w.colors["gray-700"]};
  word-break: keep-all;

  @media (max-width: 768px) {
    width: ${({variant})=>"moment"===variant?"100%":"90%"};
  }
`,SkeletonCardTitle=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`,SkeletonTitleRow=emotion_styled_browser_esm.A.div`
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
`,SkeletonSection=(emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
`,emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }
`),SkeletonSectionHeader=emotion_styled_browser_esm.A.div`
  display: flex;
  align-items: center;
  gap: 8px;
`,SkeletonSimpleCard=(emotion_styled_browser_esm.A.div`
  display: flex;
  align-items: center;
  gap: 8px;
`,emotion_styled_browser_esm.A.div`
  display: flex;
  width: 100%;
  padding: 10px 16px;
  background-color: ${theme.w.colors["gray-600_20"]};
  border-radius: 5px;
  height: ${theme.w.typography.textAreaHeight.small};
  border: 1px solid ${theme.w.colors["gray-700"]};
  align-items: center;
`),SkeletonYellowCard=emotion_styled_browser_esm.A.div`
  display: flex;
  width: 100%;
  padding: 10px 16px;
  background-color: ${theme.w.colors["yellow-300_10"]};
  border-radius: 5px;
  height: ${theme.w.typography.textAreaHeight.small};
  border: 1px solid ${theme.w.colors["gray-700"]};
  align-items: center;
`,SkeletonCardAction=emotion_styled_browser_esm.A.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`,SkeletonActionButtons=emotion_styled_browser_esm.A.div`
  display: flex;
  gap: 8px;
`,SkeletonEmojiContainer=emotion_styled_browser_esm.A.div`
  display: flex;
  gap: 8px;
`,SkeletonMomentContent=emotion_styled_browser_esm.A.div`
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
`,SkeletonRewardHistoryTable=emotion_styled_browser_esm.A.table`
  display: flex;
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
  border: 1px solid ${theme.w.colors["gray-700"]};
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 10px;

  th, td {
`,CommonSkeletonCard=({variant="moment"})=>(0,jsx_runtime.jsxs)(SkeletonCard,{variant,children:[(0,jsx_runtime.jsxs)(SkeletonCardTitle,{children:[(0,jsx_runtime.jsxs)(SkeletonTitleRow,{children:[(0,jsx_runtime.jsx)(Skeleton,{width:"16px",height:"16px",borderRadius:"50%"}),(0,jsx_runtime.jsx)(Skeleton,{width:"120px",height:"16px"})]}),(0,jsx_runtime.jsx)(SkeletonText,{lines:2,lineHeight:"18px"})]}),"moment"===variant&&(0,jsx_runtime.jsx)(SkeletonMomentContent,{children:(0,jsx_runtime.jsx)(SkeletonText,{lines:3,lineHeight:"18px"})}),"comment"===variant&&(0,jsx_runtime.jsxs)(jsx_runtime.Fragment,{children:[(0,jsx_runtime.jsxs)(SkeletonSection,{children:[(0,jsx_runtime.jsxs)(SkeletonSectionHeader,{children:[(0,jsx_runtime.jsx)(Skeleton,{width:"20px",height:"20px",borderRadius:"50%"}),(0,jsx_runtime.jsx)(Skeleton,{width:"100px",height:"16px"})]}),(0,jsx_runtime.jsx)(SkeletonSimpleCard,{children:(0,jsx_runtime.jsx)(SkeletonText,{lines:2,lineHeight:"16px"})})]}),(0,jsx_runtime.jsxs)(SkeletonSection,{children:[(0,jsx_runtime.jsxs)(SkeletonSectionHeader,{children:[(0,jsx_runtime.jsx)(Skeleton,{width:"20px",height:"20px",borderRadius:"50%"}),(0,jsx_runtime.jsx)(Skeleton,{width:"120px",height:"16px"})]}),(0,jsx_runtime.jsx)(SkeletonYellowCard,{children:(0,jsx_runtime.jsx)(SkeletonText,{lines:1,lineHeight:"16px"})})]}),(0,jsx_runtime.jsxs)(SkeletonSection,{children:[(0,jsx_runtime.jsxs)(SkeletonSectionHeader,{children:[(0,jsx_runtime.jsx)(Skeleton,{width:"20px",height:"20px",borderRadius:"50%"}),(0,jsx_runtime.jsx)(Skeleton,{width:"100px",height:"16px"})]}),(0,jsx_runtime.jsxs)(SkeletonEmojiContainer,{children:[(0,jsx_runtime.jsx)(Skeleton,{width:"32px",height:"32px",borderRadius:"50%"}),(0,jsx_runtime.jsx)(Skeleton,{width:"32px",height:"32px",borderRadius:"50%"}),(0,jsx_runtime.jsx)(Skeleton,{width:"32px",height:"32px",borderRadius:"50%"})]})]}),(0,jsx_runtime.jsx)(SkeletonCardAction,{children:(0,jsx_runtime.jsxs)(SkeletonActionButtons,{children:[(0,jsx_runtime.jsx)(Skeleton,{width:"40px",height:"32px",borderRadius:"20px"}),(0,jsx_runtime.jsx)(Skeleton,{width:"40px",height:"32px",borderRadius:"20px"}),(0,jsx_runtime.jsx)(Skeleton,{width:"40px",height:"32px",borderRadius:"20px"})]})})]}),"rewardHistory"===variant&&(0,jsx_runtime.jsxs)(SkeletonRewardHistoryTable,{children:[(0,jsx_runtime.jsx)("thead",{children:(0,jsx_runtime.jsxs)("tr",{children:[(0,jsx_runtime.jsx)("th",{children:(0,jsx_runtime.jsx)(Skeleton,{width:"100px",height:"16px"})}),(0,jsx_runtime.jsx)("th",{children:(0,jsx_runtime.jsx)(Skeleton,{width:"100px",height:"16px"})})]})}),(0,jsx_runtime.jsx)("tbody",{children:Array.from({length:10},(_,index)=>`skeleton-row-${index}`).map(uniqueKey=>(0,jsx_runtime.jsx)("tr",{children:(0,jsx_runtime.jsx)("td",{children:(0,jsx_runtime.jsx)(Skeleton,{width:"100px",height:"16px"})})},uniqueKey))})]})]});CommonSkeletonCard.__docgenInfo={description:"",methods:[],displayName:"CommonSkeletonCard",props:{variant:{required:!1,tsType:{name:"union",raw:"'moment' | 'comment' | 'rewardHistory'",elements:[{name:"literal",value:"'moment'"},{name:"literal",value:"'comment'"},{name:"literal",value:"'rewardHistory'"}]},description:"",defaultValue:{value:"'moment'",computed:!1}}}}},"./src/shared/ui/textArea/TextArea.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{f:()=>TextArea_TextArea});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const TextAreaStyles_textarea=(theme,$height)=>`\n    width: 100%;\n    padding: 20px;\n    background-color: ${theme.colors["gray-600_20"]};\n    border-radius: 5px;\n    height: ${theme.typography.textAreaHeight[$height]};\n    color: ${theme.colors.white};\n    border: 1px solid ${theme.colors["gray-700"]};\n    resize: none; \n    \n    &::placeholder {\n        color: ${theme.colors.white};\n    }\n    `,TextArea=emotion_styled_browser_esm.A.textarea`
  ${({theme,$height})=>TextAreaStyles_textarea(theme,$height)}
`,TextArea_TextArea=({placeholder,height,...props})=>(0,jsx_runtime.jsx)(TextArea,{placeholder,$height:height,...props});TextArea_TextArea.__docgenInfo={description:"",methods:[],displayName:"TextArea",props:{placeholder:{required:!0,tsType:{name:"string"},description:""},height:{required:!0,tsType:{name:"union",raw:"'small' | 'medium' | 'large'",elements:[{name:"literal",value:"'small'"},{name:"literal",value:"'medium'"},{name:"literal",value:"'large'"}]},description:""}}}},"./src/shared/ui/toast/Toast.styles.ts":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{FH:()=>ToastMessage,Jn:()=>CloseButton,N9:()=>ToastContainer,We:()=>ToastIconWrapper,z2:()=>ToastItem});var _emotion_react__WEBPACK_IMPORTED_MODULE_1__=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),_emotion_styled__WEBPACK_IMPORTED_MODULE_0__=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const slideIn=_emotion_react__WEBPACK_IMPORTED_MODULE_1__.i7`
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
`,slideOut=_emotion_react__WEBPACK_IMPORTED_MODULE_1__.i7`
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
`,toastVariants={success:theme=>`\n    background-color: ${theme.colors["emerald-50"]};\n    border-left: 4px solid ${theme.colors["emerald-500"]};\n    color: ${theme.colors["emerald-600"]};\n  `,error:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["red-500"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["red-500"]};\n    color: ${theme.colors["red-500"]};\n  `,message:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["yellow-300_80"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["yellow-300_80"]};\n    color: ${theme.colors["yellow-300_80"]};\n  `},ToastContainer=_emotion_styled__WEBPACK_IMPORTED_MODULE_0__.A.div`
  position: fixed;
  top: 10vh;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
  width: 100%;

  @media (max-width: 768px) {
    right: 10px;
    left: 10px;
    max-width: none;
    top: 8vh;
  }
`,ToastItem=_emotion_styled__WEBPACK_IMPORTED_MODULE_0__.A.div`
  ${({theme,variant})=>toastVariants[variant](theme)};
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  animation: ${({isExiting})=>isExiting?slideOut:slideIn} 0.3s ease-out forwards;
  backdrop-filter: blur(8px);
  min-height: 60px;

  ${({$isClickable})=>$isClickable&&"\n    cursor: pointer;\n    transition: transform 0.2s ease, box-shadow 0.2s ease;\n    \n    &:hover {\n      transform: translateY(-2px);\n      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);\n    }\n    \n    &:active {\n      transform: translateY(0);\n    }\n  "}

  @media (max-width: 768px) {
    padding: 14px 16px;
    font-size: 13px;
    min-height: 56px;
  }
`,ToastIconWrapper=_emotion_styled__WEBPACK_IMPORTED_MODULE_0__.A.div`
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
`,ToastMessage=_emotion_styled__WEBPACK_IMPORTED_MODULE_0__.A.div`
  flex: 1;
  line-height: 1.4;
`,CloseButton=_emotion_styled__WEBPACK_IMPORTED_MODULE_0__.A.button`
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.7;
  transition: opacity 0.2s ease;
  flex-shrink: 0;

  &:hover {
    opacity: 1;
  }

  &:focus {
    outline: 2px solid currentColor;
    outline-offset: 2px;
  }
`}}]);
//# sourceMappingURL=300.2fdf05fd.iframe.bundle.js.map