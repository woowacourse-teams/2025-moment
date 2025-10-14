"use strict";(self.webpackChunkclient=self.webpackChunkclient||[]).push([[811],{"./src/shared/store/toast.ts":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{b:()=>toasts,Y:()=>useToasts});var react=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js");const toastStore=function createStore(initialState){let state=initialState;const listeners=new Set;return{getState:()=>state,setState:value=>{const newState="function"==typeof value?value(state):value;newState!==state&&(state=newState,listeners.forEach(listener=>listener()))},subscribe:callback=>(listeners.add(callback),()=>listeners.delete(callback))}}({toasts:[]}),timers=new Map,useToasts=()=>function useStore(store){return(0,react.useSyncExternalStore)(store.subscribe,store.getState,store.getState)}(toastStore);function addToast(toast){const id=`toast-${Date.now()}-${Math.random().toString(36).slice(2,11)}`,newToast={...toast,id};toastStore.setState(state=>({toasts:[...state.toasts,newToast]}));const duration=toast.duration??3e3;if(duration>0){const prev=timers.get(id);prev&&clearTimeout(prev);const handle=setTimeout(()=>{timers.delete(id),removeToast(id)},duration);timers.set(id,handle)}return id}function removeToast(id){const t=timers.get(id);t&&(clearTimeout(t),timers.delete(id)),toastStore.setState(state=>({toasts:state.toasts.filter(t=>t.id!==id)}))}const toasts={success:(message,duration)=>addToast({message,variant:"success",duration}),error:(message,duration)=>addToast({message,variant:"error",duration}),warning:(message,duration)=>addToast({message,variant:"warning",duration}),message:(message,routeType,duration)=>addToast({message,variant:"message",routeType,duration}),hide:id=>removeToast(id),clear:()=>{timers.forEach(t=>clearTimeout(t)),timers.clear(),toastStore.setState({toasts:[]})}}},"./src/shared/ui/toast/Toast.stories.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,{Default:()=>Default,__namedExportsOrder:()=>__namedExportsOrder,default:()=>__WEBPACK_DEFAULT_EXPORT__});var react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),_Toast__WEBPACK_IMPORTED_MODULE_1__=__webpack_require__("./src/shared/ui/toast/Toast.tsx"),_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__=__webpack_require__("./src/shared/store/toast.ts");__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js");const __WEBPACK_DEFAULT_EXPORT__={title:"Shared/Toast",parameters:{layout:"centered"}},ToastExample=()=>(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsxs)(react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.Fragment,{children:[(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(_Toast__WEBPACK_IMPORTED_MODULE_1__.y,{}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsxs)("div",{style:{display:"flex",gap:"16px",flexDirection:"column"},children:[(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.success("성공적으로 처리되었습니다!"),style:{padding:"12px 24px",backgroundColor:"#10B981",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"성공 메시지 보기"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.error("오류가 발생했습니다. 다시 시도해주세요."),style:{padding:"12px 24px",backgroundColor:"#EF4444",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"에러 메시지 보기"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>{_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.success("첫 번째 메시지"),setTimeout(()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.error("두 번째 메시지"),500),setTimeout(()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.success("세 번째 메시지"),1e3)},style:{padding:"12px 24px",backgroundColor:"#6366F1",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"연속 메시지 (교체됨)"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.success("이 메시지는 10초 후에 사라집니다",1e4),style:{padding:"12px 24px",backgroundColor:"#F59E0B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"긴 지속 시간 메시지"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.message("나의 모멘트에 코멘트가 달렸습니다!","moment"),style:{padding:"12px 24px",backgroundColor:"#8B5CF6",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"모멘트 알림 메시지 (클릭 가능)"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.message("나의 코멘트에 에코가 달렸습니다! 별조각 3개를 획득했습니다!","comment"),style:{padding:"12px 24px",backgroundColor:"#EC4899",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"코멘트 알림 메시지 (클릭 가능)"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.message("일반 알림 메시지입니다"),style:{padding:"12px 24px",backgroundColor:"#64748B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"일반 메시지 (클릭 불가)"}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)("button",{onClick:()=>{_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.warning("업로드 중..."),setTimeout(()=>{_shared_store_toast__WEBPACK_IMPORTED_MODULE_2__.b.success("업로드 완료!")},2e3)},style:{padding:"12px 24px",backgroundColor:"#8B5A2B",color:"white",border:"none",borderRadius:"8px",cursor:"pointer"},children:"업로드 시뮬레이션 (순차 메시지)"})]})]}),Default={render:()=>(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(ToastExample,{})},__namedExportsOrder=["Default"];Default.parameters={...Default.parameters,docs:{...Default.parameters?.docs,source:{originalSource:"{\n  render: () => <ToastExample />\n}",...Default.parameters?.docs?.source}}}},"./src/shared/ui/toast/Toast.styles.ts":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{FH:()=>ToastMessage,Jn:()=>CloseButton,N9:()=>ToastContainer,We:()=>ToastIconWrapper,z2:()=>ToastItem});var _emotion_react__WEBPACK_IMPORTED_MODULE_1__=__webpack_require__("./node_modules/.pnpm/@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0/node_modules/@emotion/react/dist/emotion-react.browser.esm.js"),_emotion_styled__WEBPACK_IMPORTED_MODULE_0__=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const slideIn=_emotion_react__WEBPACK_IMPORTED_MODULE_1__.i7`
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
`,toastVariants={success:theme=>`\n    background-color: ${theme.colors["emerald-50"]};\n    border-left: 4px solid ${theme.colors["emerald-500"]};\n    color: ${theme.colors["emerald-600"]};\n  `,error:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["red-500"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["red-500"]};\n    color: ${theme.colors["red-500"]};\n  `,warning:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["orange-500_80"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["orange-500_80"]};\n    color: ${theme.colors["orange-500_80"]};\n  `,message:theme=>`\n    background-color: color-mix(in srgb, ${theme.colors["yellow-300_80"]} 10%, transparent);\n    border-left: 4px solid ${theme.colors["yellow-300_80"]};\n    color: ${theme.colors["yellow-300_80"]};\n  `},ToastContainer=_emotion_styled__WEBPACK_IMPORTED_MODULE_0__.A.div`
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
`},"./src/shared/ui/toast/Toast.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.d(__webpack_exports__,{y:()=>Toast});var react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),_shared_store_toast__WEBPACK_IMPORTED_MODULE_1__=__webpack_require__("./src/shared/store/toast.ts"),lucide_react__WEBPACK_IMPORTED_MODULE_6__=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-check-big.js"),lucide_react__WEBPACK_IMPORTED_MODULE_7__=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/circle-x.js"),lucide_react__WEBPACK_IMPORTED_MODULE_8__=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/key-round.js"),lucide_react__WEBPACK_IMPORTED_MODULE_9__=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/mail.js"),lucide_react__WEBPACK_IMPORTED_MODULE_10__=__webpack_require__("./node_modules/.pnpm/lucide-react@0.525.0_react@19.1.0/node_modules/lucide-react/dist/esm/icons/x.js"),react__WEBPACK_IMPORTED_MODULE_2__=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js"),react_router__WEBPACK_IMPORTED_MODULE_5__=__webpack_require__("./node_modules/.pnpm/react-router@7.6.3_react-dom@19.1.0_react@19.1.0__react@19.1.0/node_modules/react-router/dist/development/chunk-QMGIS6GS.mjs"),react_dom__WEBPACK_IMPORTED_MODULE_3__=__webpack_require__("./node_modules/.pnpm/react-dom@19.1.0_react@19.1.0/node_modules/react-dom/index.js"),_Toast_styles__WEBPACK_IMPORTED_MODULE_4__=__webpack_require__("./src/shared/ui/toast/Toast.styles.ts");const ToastItem=({toast,onClose})=>{const[isExiting,setIsExiting]=(0,react__WEBPACK_IMPORTED_MODULE_2__.useState)(!1),navigate=(0,react_router__WEBPACK_IMPORTED_MODULE_5__.Zp)(),handleClose=e=>{e?.stopPropagation(),setIsExiting(!0),setTimeout(()=>{onClose(toast.id)},300)};return(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsxs)(_Toast_styles__WEBPACK_IMPORTED_MODULE_4__.z2,{variant:toast.variant,isExiting,onClick:()=>{if("message"===toast.variant&&toast.routeType){const route="moment"===toast.routeType?"/collection/my-moment":"/collection/my-comment";navigate(route),handleClose()}},$isClickable:"message"===toast.variant&&!!toast.routeType,children:[(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(_Toast_styles__WEBPACK_IMPORTED_MODULE_4__.We,{children:(()=>{switch(toast.variant){case"success":return(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(lucide_react__WEBPACK_IMPORTED_MODULE_6__.A,{size:20});case"error":return(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(lucide_react__WEBPACK_IMPORTED_MODULE_7__.A,{size:20});case"warning":return(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(lucide_react__WEBPACK_IMPORTED_MODULE_8__.A,{size:20});case"message":return(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(lucide_react__WEBPACK_IMPORTED_MODULE_9__.A,{size:20});default:return null}})()}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(_Toast_styles__WEBPACK_IMPORTED_MODULE_4__.FH,{children:toast.message}),(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(_Toast_styles__WEBPACK_IMPORTED_MODULE_4__.Jn,{onClick:e=>handleClose(e),"aria-label":"토스트 닫기",children:(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(lucide_react__WEBPACK_IMPORTED_MODULE_10__.A,{size:16})})]})},Toast=()=>{const{toasts:activeToasts}=(0,_shared_store_toast__WEBPACK_IMPORTED_MODULE_1__.Y)();return 0===activeToasts.length?null:(0,react_dom__WEBPACK_IMPORTED_MODULE_3__.createPortal)((0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(_Toast_styles__WEBPACK_IMPORTED_MODULE_4__.N9,{children:activeToasts.map(toast=>(0,react_jsx_runtime__WEBPACK_IMPORTED_MODULE_0__.jsx)(ToastItem,{toast,onClose:_shared_store_toast__WEBPACK_IMPORTED_MODULE_1__.b.hide},toast.id))}),document.body)}}}]);
//# sourceMappingURL=shared-ui-toast-Toast-stories.938d35e9.iframe.bundle.js.map