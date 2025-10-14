"use strict";(self.webpackChunkclient=self.webpackChunkclient||[]).push([[811],{"./src/shared/ui/toast/Toast.stories.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,{Default:()=>Default,__namedExportsOrder:()=>__namedExportsOrder,default:()=>Toast_stories});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),react=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js");const toastStore=function createStore(initialState){let state=initialState;const listeners=new Set;return{getState:()=>state,setState:value=>{const newState="function"==typeof value?value(state):value;newState!==state&&(state=newState,listeners.forEach(listener=>listener()))},subscribe:callback=>(listeners.add(callback),()=>listeners.delete(callback))}}({toasts:[]}),timers=new Map,useToasts=()=>function useStore(store){return(0,react.useSyncExternalStore)(store.subscribe,store.getState,store.getState)}(toastStore);function addToast(toast){const id=`toast-${Date.now()}-${Math.random().toString(36).slice(2,11)}`,newToast={...toast,id};toastStore.setState(state=>({toasts:[...state.toasts,newToast]}));const duration=toast.duration??3e3;if(duration>0){const prev=timers.get(id);prev&&clearTimeout(prev);const handle=setTimeout(()=>{timers.delete(id),removeToast(id)},duration);timers.set(id,handle)}return id}function removeToast(id){const t=timers.get(id);t&&(clearTimeout(t),timers.delete(id)),toastStore.setState(state=>({toasts:state.toasts.filter(t=>t.id!==id)}))}const toasts={success:(message,duration)=>addToast({message,variant:"success",duration}),error:(message,duration)=>addToast({message,variant:"error",duration}),warning:(message,duration)=>addToast({message,variant:"warning",duration}),message:(message,routeType,duration)=>addToast({message,variant:"message",routeType,duration}),hide:id=>removeToast(id),clear:()=>{timers.forEach(t=>clearTimeout(t)),timers.clear(),toastStore.setState({toasts:[]})}};var circle_check_big=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-check-big.js"),circle_x=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-x.js"),key_round=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/key-round.js"),mail=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/mail.js"),x=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/x.js"),chunk_QMGIS6GS=__webpack_require__("./node_modules/.pnpm/react-router@7.6.3_react-dom@19.1.0_react@19.1.0__react@19.1.0/node_modules/react-router/dist/development/chunk-QMGIS6GS.mjs"),react_dom=__webpack_require__("./node_modules/.pnpm/react-dom@19.1.0_react@19.1.0/node_modules/react-dom/index.js"),emotion_react_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const slideIn=emotion_react_browser_esm.i7`
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
`,Toast_ToastItem=({toast,onClose})=>{const[isExiting,setIsExiting]=(0,react.useState)(!1),navigate=(0,chunk_QMGIS6GS.Zp)(),handleClose=e=>{e?.stopPropagation(),setIsExiting(!0),setTimeout(()=>{onClose(toast.id)},300)};return(0,jsx_runtime.jsxs)(ToastItem,{variant:toast.variant,isExiting,onClick:()=>{if("message"===toast.variant&&toast.routeType){const route="moment"===toast.routeType?"/collection/my-moment":"/collection/my-comment";navigate(route),handleClose()}},$isClickable:"message"===toast.variant&&!!toast.routeType,children:[(0,jsx_runtime.jsx)(ToastIconWrapper,{children:(()=>{switch(toast.variant){case"success":return(0,jsx_runtime.jsx)(circle_check_big.A,{size:20});case"error":return(0,jsx_runtime.jsx)(circle_x.A,{size:20});case"warning":return(0,jsx_runtime.jsx)(key_round.A,{size:20});case"message":return(0,jsx_runtime.jsx)(mail.A,{size:20});default:return null}})()}),(0,jsx_runtime.jsx)(ToastMessage,{children:toast.message}),(0,jsx_runtime.jsx)(CloseButton,{onClick:e=>handleClose(e),"aria-label":"토스트 닫기",children:(0,jsx_runtime.jsx)(x.A,{size:16})})]})},Toast=()=>{const{toasts:activeToasts}=useToasts();return 0===activeToasts.length?null:(0,react_dom.createPortal)((0,jsx_runtime.jsx)(ToastContainer,{children:activeToasts.map(toast=>(0,jsx_runtime.jsx)(Toast_ToastItem,{toast,onClose:toasts.hide},toast.id))}),document.body)},Toast_stories={title:"Shared/Toast",parameters:{layout:"centered"}},ToastExample=()=>(0,jsx_runtime.jsxs)(jsx_runtime.Fragment,{children:[(0,jsx_runtime.jsx)(Toast,{}),(0,jsx_runtime.jsxs)("div",{style:{display:"flex",gap:"16px",flexDirection:"column"},children:[(0,jsx_runtime.jsx)("button",{onClick:()=>toasts.success("성공적으로 처리되었습니다!"),style:{padding:"12px 24px",backgroundColor:"#10B981",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"성공 메시지 보기"}),(0,jsx_runtime.jsx)("button",{onClick:()=>toasts.error("오류가 발생했습니다. 다시 시도해주세요."),style:{padding:"12px 24px",backgroundColor:"#EF4444",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"에러 메시지 보기"}),(0,jsx_runtime.jsx)("button",{onClick:()=>{toasts.success("첫 번째 메시지"),setTimeout(()=>toasts.error("두 번째 메시지"),500),setTimeout(()=>toasts.success("세 번째 메시지"),1e3)},style:{padding:"12px 24px",backgroundColor:"#6366F1",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"연속 메시지 (교체됨)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>toasts.success("이 메시지는 10초 후에 사라집니다",1e4),style:{padding:"12px 24px",backgroundColor:"#F59E0B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"긴 지속 시간 메시지"}),(0,jsx_runtime.jsx)("button",{onClick:()=>toasts.message("나의 모멘트에 코멘트가 달렸습니다!","moment"),style:{padding:"12px 24px",backgroundColor:"#8B5CF6",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"모멘트 알림 메시지 (클릭 가능)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>toasts.message("나의 코멘트에 에코가 달렸습니다! 별조각 3개를 획득했습니다!","comment"),style:{padding:"12px 24px",backgroundColor:"#EC4899",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"코멘트 알림 메시지 (클릭 가능)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>toasts.message("일반 알림 메시지입니다"),style:{padding:"12px 24px",backgroundColor:"#64748B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"일반 메시지 (클릭 불가)"}),(0,jsx_runtime.jsx)("button",{onClick:()=>{toasts.warning("업로드 중..."),setTimeout(()=>{toasts.success("업로드 완료!")},2e3)},style:{padding:"12px 24px",backgroundColor:"#8B5A2B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"업로드 시뮬레이션 (순차 메시지)"})]})]}),Default={render:()=>(0,jsx_runtime.jsx)(ToastExample,{})},__namedExportsOrder=["Default"];Default.parameters={...Default.parameters,docs:{...Default.parameters?.docs,source:{originalSource:"{\n  render: () => <ToastExample />\n}",...Default.parameters?.docs?.source}}}}}]);
//# sourceMappingURL=shared-ui-toast-Toast-stories.8619762c.iframe.bundle.js.map