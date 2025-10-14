/*! For license information please see shared-ui-notFound-NotFound-stories.a98081fc.iframe.bundle.js.LICENSE.txt */
"use strict";(self.webpackChunkclient=self.webpackChunkclient||[]).push([[651],{"./src/shared/ui/notFound/NotFound.stories.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,{Default:()=>Default,Small:()=>Small,SmallWithCard:()=>SmallWithCard,WithAlertIcon:()=>WithAlertIcon,WithCard:()=>WithCard,WithSearchIcon:()=>WithSearchIcon,__namedExportsOrder:()=>__namedExportsOrder,default:()=>NotFound_stories});var createLucideIcon=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/createLucideIcon.js");const Eye=(0,createLucideIcon.A)("eye",[["path",{d:"M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0",key:"1nclc0"}],["circle",{cx:"12",cy:"12",r:"3",key:"1v7zrd"}]]),Search=(0,createLucideIcon.A)("search",[["path",{d:"m21 21-4.34-4.34",key:"14j7rj"}],["circle",{cx:"11",cy:"11",r:"8",key:"4ej97u"}]]),CircleAlert=(0,createLucideIcon.A)("circle-alert",[["circle",{cx:"12",cy:"12",r:"10",key:"1mglay"}],["line",{x1:"12",x2:"12",y1:"8",y2:"12",key:"1pkeuh"}],["line",{x1:"12",x2:"12.01",y1:"16",y2:"16",key:"4dfq90"}]]);var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),Card=__webpack_require__("./src/shared/ui/card/Card.tsx"),react=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js"),Skeleton=__webpack_require__("./src/shared/ui/skeleton/Skeleton.tsx"),emotion_react_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const LazyImageContainer=emotion_styled_browser_esm.A.div`
  position: relative;
  display: inline-block;
`,blackHoleStyles=emotion_react_browser_esm.AH`
  width: 150px;
  height: 150px;
`,iconStyles=emotion_react_browser_esm.AH`
  width: 40px;
  height: 40px;
`,characterStyles=emotion_react_browser_esm.AH`
  width: 250px;
  object-fit: contain;

  @media (max-width: 768px) {
    width: 180px;
  }
`,levelIconStyles=emotion_react_browser_esm.AH`
  width: 50px;
  height: 50px;
  object-fit: contain;

  @media (max-width: 768px) {
    width: 25px;
    height: 25px;
  }
`,ErrorFallback=emotion_styled_browser_esm.A.div`
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: ${({$borderRadius})=>$borderRadius||"4px"};
  width: ${({width})=>"number"==typeof width?`${width}px`:width||"100%"};
  height: ${({height})=>"number"==typeof height?`${height}px`:height||"100%"};

  ${({$variant})=>{switch($variant){case"blackHole":return blackHoleStyles;case"icon":return iconStyles;case"character":return characterStyles;case"levelIcon":return levelIconStyles;default:return""}}}
`,ErrorIcon=emotion_styled_browser_esm.A.span`
  font-size: 24px;
  opacity: 0.5;
`,PlaceholderWrapper=emotion_styled_browser_esm.A.div`
  position: absolute;
  top: 0;
  left: 0;
  opacity: ${({$isVisible})=>$isVisible?1:0};
  transition: opacity 0.3s ease;
  pointer-events: none;
`,ErrorWrapper=emotion_styled_browser_esm.A.div`
  position: absolute;
  top: 0;
  left: 0;
  opacity: ${({$isVisible})=>$isVisible?1:0};
  transition: opacity 0.3s ease;
  pointer-events: ${({$isVisible})=>$isVisible?"auto":"none"};
`,Image=emotion_styled_browser_esm.A.img`
  opacity: ${({$isVisible})=>$isVisible?1:0};
  transition: opacity 0.3s ease;

  ${({$variant})=>{switch($variant){case"blackHole":return blackHoleStyles;case"icon":return iconStyles;case"character":return characterStyles;case"levelIcon":return levelIconStyles;default:return""}}}
`,LazyImage=({src,alt,width,height,borderRadius="4px",skeletonClassName,variant="default",eager=!1,...imgProps})=>{const[isLoaded,setIsLoaded]=(0,react.useState)(!1),[hasError,setHasError]=(0,react.useState)(!1);return(0,jsx_runtime.jsxs)(LazyImageContainer,{children:["blackHole"!==variant&&(0,jsx_runtime.jsx)(PlaceholderWrapper,{$isVisible:!isLoaded&&!hasError,children:(0,jsx_runtime.jsx)(Skeleton.E,{width,height,borderRadius,className:skeletonClassName})}),(0,jsx_runtime.jsx)(ErrorWrapper,{$isVisible:hasError,children:(0,jsx_runtime.jsx)(ErrorFallback,{width,height,$borderRadius:borderRadius,$variant:variant,children:(0,jsx_runtime.jsx)(ErrorIcon,{children:"📷"})})}),(0,jsx_runtime.jsx)(Image,{...imgProps,src,alt,loading:eager?"eager":"lazy",onLoad:()=>{setIsLoaded(!0)},onError:()=>{setHasError(!0)},$variant:variant,$isVisible:isLoaded&&!hasError,style:imgProps.style})]})};LazyImage.__docgenInfo={description:"",methods:[],displayName:"LazyImage",props:{src:{required:!0,tsType:{name:"string"},description:""},alt:{required:!0,tsType:{name:"string"},description:""},width:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:""},height:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:""},borderRadius:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'4px'",computed:!1}},skeletonClassName:{required:!1,tsType:{name:"string"},description:""},variant:{required:!1,tsType:{name:"union",raw:"'blackHole' | 'icon' | 'character' | 'levelIcon' | 'default'",elements:[{name:"literal",value:"'blackHole'"},{name:"literal",value:"'icon'"},{name:"literal",value:"'character'"},{name:"literal",value:"'levelIcon'"},{name:"literal",value:"'default'"}]},description:"",defaultValue:{value:"'default'",computed:!1}},eager:{required:!1,tsType:{name:"boolean"},description:"",defaultValue:{value:"false",computed:!1}}},composes:["Omit"]};const NotFoundWrapper=emotion_styled_browser_esm.A.div`
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
`,NotFound=(emotion_styled_browser_esm.A.img`
  width: 250px;
  object-fit: contain;

  @media (max-width: 768px) {
    width: 180px;
  }
`,({title,subtitle,icon:IconComponent=Eye,iconSize=24,size="large",withCard=!1})=>{const content=(0,jsx_runtime.jsxs)(NotFoundWrapper,{$size:size,children:[(0,jsx_runtime.jsx)(NotFoundIconWrapper,{$size:size,children:(0,jsx_runtime.jsx)(IconComponent,{size:iconSize})}),(0,jsx_runtime.jsxs)(NotFoundContainer,{$size:size,children:[(0,jsx_runtime.jsx)(NotFoundTitle,{$size:size,children:title}),(0,jsx_runtime.jsx)(NotFoundSubtitle,{$size:size,children:subtitle}),(0,jsx_runtime.jsx)(LazyImage,{src:"/images/character.webp",alt:"notFound",variant:"character",width:"250px",height:"auto"})]})]});return withCard?(0,jsx_runtime.jsx)(Card.Z,{width:"medium",children:content}):content});NotFound.__docgenInfo={description:"",methods:[],displayName:"NotFound",props:{title:{required:!0,tsType:{name:"string"},description:""},subtitle:{required:!0,tsType:{name:"string"},description:""},icon:{required:!1,tsType:{name:"LucideIcon"},description:"",defaultValue:{value:"Eye",computed:!0}},iconSize:{required:!1,tsType:{name:"number"},description:"",defaultValue:{value:"24",computed:!1}},size:{required:!1,tsType:{name:"union",raw:"'small' | 'large'",elements:[{name:"literal",value:"'small'"},{name:"literal",value:"'large'"}]},description:"",defaultValue:{value:"'large'",computed:!1}},withCard:{required:!1,tsType:{name:"boolean"},description:"",defaultValue:{value:"false",computed:!1}}}};const NotFound_stories={title:"Shared/NotFound",component:NotFound,argTypes:{size:{control:{type:"radio"},options:["small","large"]},withCard:{control:{type:"boolean"}},iconSize:{control:{type:"number"}}},args:{title:"페이지를 찾을 수 없습니다",subtitle:"요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다",icon:Eye,iconSize:24,size:"large",withCard:!1}},Default={},Small={args:{size:"small",title:"검색 결과 없음",subtitle:"검색 조건을 변경해보세요"}},WithCard={args:{withCard:!0,title:"데이터가 없습니다",subtitle:"아직 등록된 데이터가 없습니다"}},WithSearchIcon={args:{icon:Search,title:"검색 결과가 없습니다",subtitle:"다른 키워드로 검색해보세요"}},WithAlertIcon={args:{icon:CircleAlert,title:"오류가 발생했습니다",subtitle:"잠시 후 다시 시도해주세요",iconSize:32}},SmallWithCard={args:{size:"small",withCard:!0,title:"빈 상태",subtitle:"데이터가 없습니다"}},__namedExportsOrder=["Default","Small","WithCard","WithSearchIcon","WithAlertIcon","SmallWithCard"];Default.parameters={...Default.parameters,docs:{...Default.parameters?.docs,source:{originalSource:"{}",...Default.parameters?.docs?.source}}},Small.parameters={...Small.parameters,docs:{...Small.parameters?.docs,source:{originalSource:"{\n  args: {\n    size: 'small',\n    title: '검색 결과 없음',\n    subtitle: '검색 조건을 변경해보세요'\n  }\n}",...Small.parameters?.docs?.source}}},WithCard.parameters={...WithCard.parameters,docs:{...WithCard.parameters?.docs,source:{originalSource:"{\n  args: {\n    withCard: true,\n    title: '데이터가 없습니다',\n    subtitle: '아직 등록된 데이터가 없습니다'\n  }\n}",...WithCard.parameters?.docs?.source}}},WithSearchIcon.parameters={...WithSearchIcon.parameters,docs:{...WithSearchIcon.parameters?.docs,source:{originalSource:"{\n  args: {\n    icon: Search,\n    title: '검색 결과가 없습니다',\n    subtitle: '다른 키워드로 검색해보세요'\n  }\n}",...WithSearchIcon.parameters?.docs?.source}}},WithAlertIcon.parameters={...WithAlertIcon.parameters,docs:{...WithAlertIcon.parameters?.docs,source:{originalSource:"{\n  args: {\n    icon: AlertCircle,\n    title: '오류가 발생했습니다',\n    subtitle: '잠시 후 다시 시도해주세요',\n    iconSize: 32\n  }\n}",...WithAlertIcon.parameters?.docs?.source}}},SmallWithCard.parameters={...SmallWithCard.parameters,docs:{...SmallWithCard.parameters?.docs,source:{originalSource:"{\n  args: {\n    size: 'small',\n    withCard: true,\n    title: '빈 상태',\n    subtitle: '데이터가 없습니다'\n  }\n}",...SmallWithCard.parameters?.docs?.source}}}},"./src/shared/ui/skeleton/Skeleton.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{E:()=>Skeleton,r:()=>SkeletonText});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),theme=(__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js"),__webpack_require__("./src/app/styles/theme.ts")),emotion_react_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const shimmer=emotion_react_browser_esm.i7`
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
`,({width="100%",height="1rem",borderRadius="4px",className})=>(0,jsx_runtime.jsx)(SkeletonContainer,{width,height,borderRadius,className})),SkeletonText=({lines=1,lineHeight="1rem",gap="0.5rem"})=>(0,jsx_runtime.jsx)(SkeletonTextContainer,{gap,children:Array.from({length:lines}).map((_,index)=>(0,jsx_runtime.jsx)(Skeleton,{height:lineHeight,width:index===lines-1?"75%":"100%"},index))});Skeleton.__docgenInfo={description:"",methods:[],displayName:"Skeleton",props:{width:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'100%'",computed:!1}},height:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'1rem'",computed:!1}},borderRadius:{required:!1,tsType:{name:"union",raw:"string | number",elements:[{name:"string"},{name:"number"}]},description:"",defaultValue:{value:"'4px'",computed:!1}},className:{required:!1,tsType:{name:"string"},description:""}}},SkeletonText.__docgenInfo={description:"",methods:[],displayName:"SkeletonText",props:{lines:{required:!1,tsType:{name:"number"},description:"",defaultValue:{value:"1",computed:!1}},lineHeight:{required:!1,tsType:{name:"string"},description:"",defaultValue:{value:"'1rem'",computed:!1}},gap:{required:!1,tsType:{name:"string"},description:"",defaultValue:{value:"'0.5rem'",computed:!1}}}}}}]);
//# sourceMappingURL=shared-ui-notFound-NotFound-stories.a98081fc.iframe.bundle.js.map