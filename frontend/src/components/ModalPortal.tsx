import { createPortal } from 'react-dom';

interface ModalPortalProps {
  children: React.ReactNode;
  onClose: () => void;
}

/**
 * Renders a full-screen modal overlay via a React Portal.
 * This ensures the modal backdrop always covers the entire viewport,
 * regardless of parent scroll containers or overflow settings.
 */
export default function ModalPortal({ children, onClose }: ModalPortalProps) {
  return createPortal(
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center p-4"
      style={{ backgroundColor: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)' }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      {children}
    </div>,
    document.body
  );
}
