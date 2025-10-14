"use strict";(self.webpackChunkclient=self.webpackChunkclient||[]).push([[811],{"./src/shared/ui/toast/Toast.stories.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,{Default:()=>Default,__namedExportsOrder:()=>__namedExportsOrder,default:()=>Toast_stories});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),circle_check_big=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-check-big.js"),circle_x=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-x.js"),key_round=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/key-round.js"),mail=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/mail.js"),x=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/x.js"),react=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js"),chunk_QMGIS6GS=__webpack_require__("./node_modules/.pnpm/react-router@7.6.3_react-dom@19.1.0_react@19.1.0__react@19.1.0/node_modules/react-router/dist/development/chunk-QMGIS6GS.mjs"),emotion_react_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const slideIn=emotion_react_browser_esm.i7`
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
`,slideOut=emotion_react_browser_esm.i7`
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
`,toastVariants={success:theme=>`\n    background-color: ${theme.colors["emerald-50"]};\n    border-left: 4px solid ${theme.colors["emerald-500"]};\n    color: ${theme.colors["emerald-600"]};\n  `,error:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["red-500"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["red-500"]};\n    color: ${theme.colors["red-500"]};\n  `,warning:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["orange-500_80"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["orange-500_80"]};\n    color: ${theme.colors["orange-500_80"]};\n  `,message:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["yellow-300_80"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["yellow-300_80"]};\n    color: ${theme.colors["yellow-300_80"]};\n  `},ToastContainer=emotion_styled_browser_esm.A.div`
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
`,ToastItem=emotion_styled_browser_esm.A.div`
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
`,ToastIconWrapper=emotion_styled_browser_esm.A.div`
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
`,ToastMessage=emotion_styled_browser_esm.A.div`
  flex: 1;
  line-height: 1.4;
`,CloseButton=emotion_styled_browser_esm.A.button`
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
`,Toast=({message,variant,duration=3e3,routeType,onClose})=>{const[isExiting,setIsExiting]=(0,react.useState)(!1),navigate=(0,chunk_QMGIS6GS.Zp)();(0,react.useEffect)(()=>{if(duration>0){const timer=setTimeout(()=>{handleClose()},duration);return()=>clearTimeout(timer)}},[duration]);const handleClose=e=>{e?.stopPropagation(),setIsExiting(!0),setTimeout(()=>{onClose()},300)};return(0,jsx_runtime.jsxs)(ToastItem,{variant,isExiting,onClick:()=>{if("message"===variant&&routeType){navigate("moment"===routeType?"/collection/my-moment":"/collection/my-comment"),handleClose()}},$isClickable:"message"===variant&&!!routeType,children:[(0,jsx_runtime.jsx)(ToastIconWrapper,{children:(()=>{switch(variant){case"success":return(0,jsx_runtime.jsx)(circle_check_big.A,{size:20});case"error":return(0,jsx_runtime.jsx)(circle_x.A,{size:20});case"warning":return(0,jsx_runtime.jsx)(key_round.A,{size:20});case"message":return(0,jsx_runtime.jsx)(mail.A,{size:20});default:return null}})()}),(0,jsx_runtime.jsx)(ToastMessage,{children:message}),(0,jsx_runtime.jsx)(CloseButton,{onClick:e=>handleClose(e),"aria-label":"토스트 닫기",children:(0,jsx_runtime.jsx)(x.A,{size:16})})]})};Toast.__docgenInfo={description:"",methods:[],displayName:"Toast",props:{message:{required:!0,tsType:{name:"string"},description:""},variant:{required:!0,tsType:{name:"union",raw:"'success' | 'error' | 'warning' | 'message'",elements:[{name:"literal",value:"'success'"},{name:"literal",value:"'error'"},{name:"literal",value:"'warning'"},{name:"literal",value:"'message'"}]},description:""},duration:{required:!1,tsType:{name:"number"},description:"",defaultValue:{value:"3000",computed:!1}},routeType:{required:!1,tsType:{name:"union",raw:"'moment' | 'comment'",elements:[{name:"literal",value:"'moment'"},{name:"literal",value:"'comment'"}]},description:""},onClose:{required:!0,tsType:{name:"signature",type:"function",raw:"() => void",signature:{arguments:[],return:{name:"void"}}},description:""}}};var react_dom=__webpack_require__("./node_modules/.pnpm/react-dom@19.1.0_react@19.1.0/node_modules/react-dom/index.js");const ToastContext=(0,react.createContext)(void 0),ToastProvider=({children})=>{const[toast,setToast]=(0,react.useState)(null),removeToast=()=>{setToast(null)},contextValue={addToast:toastParams=>{setToast(toastParams)},removeToast,toast};return(0,jsx_runtime.jsxs)(ToastContext.Provider,{value:contextValue,children:[children,toast&&(0,react_dom.createPortal)((0,jsx_runtime.jsx)(ToastContainer,{children:(0,jsx_runtime.jsx)(Toast,{message:toast.message,variant:toast.variant,duration:toast.duration,routeType:toast.routeType,onClose:removeToast},"toast")}),document.body)]})};ToastProvider.__docgenInfo={description:"",methods:[],displayName:"ToastProvider",props:{children:{required:!0,tsType:{name:"ReactNode"},description:""}}};const useToast=()=>{const{addToast,removeToast,toast}=(()=>{const context=(0,react.useContext)(ToastContext);if(!context)throw new Error("useToastContext must be used within a ToastProvider");return context})();return{showSuccess:(message,duration)=>{addToast({message,variant:"success",duration})},showError:(message,duration)=>{addToast({message,variant:"error",duration})},showWarning:(message,duration)=>{addToast({message,variant:"warning",duration})},showMessage:(message,routeType,duration)=>{addToast({message,variant:"message",duration,routeType})},removeToast,toast}},Toast_stories={title:"Shared/Toast",decorators:[Story=>(0,jsx_runtime.jsx)(ToastProvider,{children:(0,jsx_runtime.jsx)(Story,{})})],parameters:{layout:"centered"}},ToastExample=()=>{const{showSuccess,showError,showMessage}=useToast();return(0,jsx_runtime.jsxs)("div",{style:{display:"flex",gap:"16px",flexDirection:"column"},children:[(0,jsx_runtime.jsx)("button",{onClick:()=>showSuccess("성공적으로 처리되었습니다!"),style:{padding:"12px 24px",backgroundColor:"#10B981",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"성공 메시지 보기"}),(0,jsx_runtime.jsx)("button",{onClick:()=>showError("오류가 발생했습니다. 다시 시도해주세요."),style:{padding:"12px 24px",backgroundColor:"#EF4444",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"에러 메시지 보기"}),(0,jsx_runtime.jsx)("button",{onClick:()=>{showSuccess("첫 번째 메시지"),setTimeout(()=>showError("두 번째 메시지"),500),setTimeout(()=>showSuccess("세 번째 메시지"),1e3)},style:{padding:"12px 24px",backgroundColor:"#6366F1",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"연속 메시지 (교체됨)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>showSuccess("이 메시지는 10초 후에 사라집니다",1e4),style:{padding:"12px 24px",backgroundColor:"#F59E0B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"긴 지속 시간 메시지"}),(0,jsx_runtime.jsx)("button",{onClick:()=>showMessage("나의 모멘트에 코멘트가 달렸습니다!","moment"),style:{padding:"12px 24px",backgroundColor:"#8B5CF6",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"모멘트 알림 메시지 (클릭 가능)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>showMessage("나의 코멘트에 에코가 달렸습니다! 별조각 3개를 획득했습니다!","comment"),style:{padding:"12px 24px",backgroundColor:"#EC4899",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"코멘트 알림 메시지 (클릭 가능)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>showMessage("일반 알림 메시지입니다"),style:{padding:"12px 24px",backgroundColor:"#64748B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"일반 메시지 (클릭 불가)"})]})},Default={render:()=>(0,jsx_runtime.jsx)(ToastExample,{})},__namedExportsOrder=["Default"];Default.parameters={...Default.parameters,docs:{...Default.parameters?.docs,source:{originalSource:"{\n  render: () => <ToastExample />\n}",...Default.parameters?.docs?.source}}}}}]);
//# sourceMappingURL=shared-ui-toast-Toast-stories.4c634c34.iframe.bundle.js.map