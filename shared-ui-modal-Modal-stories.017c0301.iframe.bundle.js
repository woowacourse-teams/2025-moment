"use strict";(self.webpackChunkclient=self.webpackChunkclient||[]).push([[703],{"./src/shared/ui/modal/Modal.stories.tsx":(__unused_webpack_module,__webpack_exports__,__webpack_require__)=>{__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,{Default:()=>Default,Memoji:()=>Memoji,__namedExportsOrder:()=>__namedExportsOrder,default:()=>Modal_stories});var jsx_runtime=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/jsx-runtime.js"),react_dom=__webpack_require__("./node_modules/.pnpm/react-dom@19.1.0_react@19.1.0/node_modules/react-dom/index.js"),emotion_styled_browser_esm=__webpack_require__("./node_modules/.pnpm/@emotion+styled@11.14.1_@emotion+react@11.14.0_@types+react@19.1.8_react@19.1.0__@types+react@19.1.8_react@19.1.0/node_modules/@emotion/styled/dist/emotion-styled.browser.esm.js");const modalFrameStyles={default:(theme,props)=>`\n    background-color: ${theme.colors["slate-800"]};\n    border-radius: 10px;\n    border: 1px solid ${theme.colors["gray-700"]};\n    padding: 20px 30px;\n    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);\n    width: ${"center"===props.$position?theme.typography.modalWidth[props.$size].desktop:"100%"};\n    height: ${props.$height||"auto"};\n\n    ${theme.mediaQueries.tablet} {\n      padding: 16px 24px;\n      width: ${"center"===props.$position?theme.typography.modalWidth[props.$size].tablet:"100%"};\n    }\n    \n    ${theme.mediaQueries.mobile} {\n      padding: 12px 20px;\n      width: ${"center"===props.$position?theme.typography.modalWidth[props.$size].mobile:"100%"};\n    }\n  `,memoji:theme=>`\n    background-color: ${theme.colors["slate-800"]};\n    border-radius: 10px;\n    border: 1px solid ${theme.colors["gray-700"]};\n    padding: 20px 30px;\n    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);\n\n    width: 520px;\n    height: 520px;\n    \n    ${theme.mediaQueries.mobile} {\n      padding: 16px 28px;\n      width: 90%;\n    }\n  `},ModalFrame=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  gap: 15px;
  position: ${({$position})=>"center"===$position?"relative":"fixed"};
  bottom: ${({$position})=>"bottom"===$position?"0":"auto"};
  left: ${({$position})=>"bottom"===$position?"50%":"auto"};
  transform: ${({$position})=>"bottom"===$position?"translateX(-50%)":"none"};

  ${({theme,variant,$size,$position,$height})=>modalFrameStyles[variant](theme,{$size,$position,$height})};
  ${({theme,externalVariant})=>externalVariant&&externalVariant(theme)};
`,ModalWrapper=emotion_styled_browser_esm.A.div`
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  width: 100dvw;
  height: 100dvh;
  background-color: ${({theme})=>theme.colors.black_70};
  color: ${({theme})=>theme.colors.white};
  z-index: 1000;
  left: 0;
  top: 0;
`,ModalCloseButton=emotion_styled_browser_esm.A.button`
  width: 30px;
  height: 30px;
  border-radius: 50%;

  &:hover {
    background-color: ${({theme})=>theme.colors["gray-700"]};
  }
`,ModalHeader=emotion_styled_browser_esm.A.div`
  display: flex;
  width: 100%;
  justify-content: ${({$hasTitle})=>$hasTitle?"space-between":"right"};
  align-items: center;
`,ModalContent=emotion_styled_browser_esm.A.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 10px;
  overflow-y: auto;
`,ModalFooter=emotion_styled_browser_esm.A.div`
  display: flex;
  /* width: 100%; */
  justify-content: flex-end;
  gap: 10px;
`;var react=__webpack_require__("./node_modules/.pnpm/react@19.1.0/node_modules/react/index.js");const hooks_useModalFocus=isOpen=>{const modalRef=(0,react.useRef)(null),focusableElementsRef=(0,react.useRef)(null);return(0,react.useEffect)(()=>{if(isOpen&&modalRef.current){focusableElementsRef.current=modalRef.current.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');const focusableElements=focusableElementsRef.current;focusableElements.length>0?focusableElements[0].focus():(modalRef.current.setAttribute("tabindex","-1"),modalRef.current.focus());const handleKeyDown=event=>{if("Tab"===event.key){if(!focusableElementsRef.current||0===focusableElementsRef.current.length)return;const focusableElements=focusableElementsRef.current,firstElement=focusableElements[0],lastElement=focusableElements[focusableElements.length-1];event.shiftKey&&document.activeElement===firstElement?(lastElement.focus(),event.preventDefault()):event.shiftKey||document.activeElement!==lastElement||(firstElement.focus(),event.preventDefault())}};return document.addEventListener("keydown",handleKeyDown),()=>document.removeEventListener("keydown",handleKeyDown)}},[isOpen]),modalRef},ModalContext=(0,react.createContext)(void 0);function Modal({children,position="center",size="medium",height,isOpen,onClose:handleClose,variant="default",externalVariant}){if(!isOpen)return null;const modalRef=hooks_useModalFocus(isOpen);return(0,react.useEffect)(()=>{const handleKeyDown=e=>{"Escape"===e.key&&handleClose()};return document.addEventListener("keydown",handleKeyDown),()=>document.removeEventListener("keydown",handleKeyDown)},[handleClose]),(0,react_dom.createPortal)((0,jsx_runtime.jsx)(ModalContext.Provider,{value:{handleClose},children:(0,jsx_runtime.jsx)(ModalWrapper,{onClick:e=>{e.target===e.currentTarget&&handleClose()},children:(0,jsx_runtime.jsx)(ModalFrame,{variant,externalVariant,role:"dialog","aria-modal":"true",$position:position,$size:size,$height:height,onClick:e=>e.stopPropagation(),ref:modalRef,children})})}),document.body)}function useModal(){const[isOpen,setIsOpen]=(0,react.useState)(!1);return{isOpen,handleOpen:()=>{setIsOpen(!0)},handleClose:()=>{setIsOpen(!1)}}}Modal.Header=({title,showCloseButton=!0})=>{const context=(0,react.useContext)(ModalContext);if(!context)throw new Error("Modal.Header는 Modal 컴포넌트 내부에서 사용되어야 합니다.");const{handleClose}=context;return(0,jsx_runtime.jsxs)(ModalHeader,{$hasTitle:!!title,children:[title,showCloseButton&&(0,jsx_runtime.jsx)(ModalCloseButton,{onClick:handleClose,children:"X"})]})},Modal.Content=({children})=>(0,jsx_runtime.jsx)(ModalContent,{children}),Modal.Footer=({children})=>(0,jsx_runtime.jsx)(ModalFooter,{children});var ui=__webpack_require__("./src/shared/ui/index.ts"),Button=__webpack_require__("./src/shared/ui/button/Button.tsx");const Modal_stories={title:"Shared/Modal",component:Modal,argTypes:{position:{control:{type:"radio"},options:["center","bottom"]},size:{control:{type:"radio"},options:["small","medium","large"]},variant:{control:{type:"radio"},options:["default","memoji"]}},args:{position:"center",size:"medium",variant:"default"}},Default={args:{position:"center",size:"small",variant:"default"},render:args=>{const{handleOpen,handleClose,isOpen}=useModal();return(0,jsx_runtime.jsxs)(jsx_runtime.Fragment,{children:[(0,jsx_runtime.jsx)(Button.$,{title:"기본 모달 열기",variant:"secondary",onClick:handleOpen}),(0,jsx_runtime.jsxs)(Modal,{position:args.position,size:args.size,variant:"default",isOpen,onClose:handleClose,children:[(0,jsx_runtime.jsx)(Modal.Header,{showCloseButton:!0}),(0,jsx_runtime.jsxs)(Modal.Content,{children:[(0,jsx_runtime.jsx)("p",{children:"기본 모달 컴포넌트입니다."}),(0,jsx_runtime.jsx)(ui.Tn,{height:"small",content:(0,jsx_runtime.jsx)("div",{children:"정말 멋진 모달이네요."})}),(0,jsx_runtime.jsx)(Button.$,{title:"공감하기",variant:"primary",onClick:handleClose})]})]})]})}},Memoji={render:args=>{const{handleOpen,handleClose,isOpen}=useModal();return(0,jsx_runtime.jsxs)(jsx_runtime.Fragment,{children:[(0,jsx_runtime.jsx)(Button.$,{title:"메모지 모달 열기",variant:"secondary",onClick:handleOpen}),(0,jsx_runtime.jsxs)(Modal,{position:args.position,variant:"memoji",isOpen,onClose:handleClose,children:[(0,jsx_runtime.jsx)(Modal.Header,{title:"메모지 모달",showCloseButton:!0}),(0,jsx_runtime.jsx)(Modal.Content,{children:(0,jsx_runtime.jsx)("p",{children:"고정 크기의 메모지 스타일 모달입니다."})})]})]})}},__namedExportsOrder=["Default","Memoji"];Default.parameters={...Default.parameters,docs:{...Default.parameters?.docs,source:{originalSource:'{\n  args: {\n    position: \'center\',\n    size: \'small\',\n    variant: \'default\'\n  },\n  render: args => {\n    const {\n      handleOpen,\n      handleClose,\n      isOpen\n    } = useModal();\n    return <>\n        <Button title="기본 모달 열기" variant="secondary" onClick={handleOpen} />\n        <Modal position={args.position} size={args.size} variant="default" isOpen={isOpen} onClose={handleClose}>\n          <Modal.Header showCloseButton={true} />\n          <Modal.Content>\n            <p>기본 모달 컴포넌트입니다.</p>\n            <SimpleCard height="small" content={<div>정말 멋진 모달이네요.</div>} />\n            <Button title="공감하기" variant="primary" onClick={handleClose} />\n          </Modal.Content>\n        </Modal>\n      </>;\n  }\n}',...Default.parameters?.docs?.source}}},Memoji.parameters={...Memoji.parameters,docs:{...Memoji.parameters?.docs,source:{originalSource:'{\n  render: args => {\n    const {\n      handleOpen,\n      handleClose,\n      isOpen\n    } = useModal();\n    return <>\n        <Button title="메모지 모달 열기" variant="secondary" onClick={handleOpen} />\n        <Modal position={args.position} variant="memoji" isOpen={isOpen} onClose={handleClose}>\n          <Modal.Header title="메모지 모달" showCloseButton={true} />\n          <Modal.Content>\n            <p>고정 크기의 메모지 스타일 모달입니다.</p>\n          </Modal.Content>\n        </Modal>\n      </>;\n  }\n}',...Memoji.parameters?.docs?.source}}}}}]);
//# sourceMappingURL=shared-ui-modal-Modal-stories.017c0301.iframe.bundle.js.map