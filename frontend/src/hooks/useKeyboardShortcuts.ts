import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

interface Shortcut {
  /** Key combination, e.g. 'ctrl+k', 'alt+d' */
  combo: string;
  /** Human-readable description */
  label: string;
  /** Action to run */
  action: () => void;
}

/**
 * Global keyboard shortcut hook.
 * Pass an array of shortcuts; they are bound on mount and unbound on unmount.
 */
export function useKeyboardShortcuts(shortcuts: Shortcut[]) {
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      for (const s of shortcuts) {
        const parts = s.combo.toLowerCase().split('+');
        const key = parts[parts.length - 1];
        const needCtrl = parts.includes('ctrl');
        const needAlt = parts.includes('alt');
        const needShift = parts.includes('shift');

        if (
          e.key.toLowerCase() === key &&
          e.ctrlKey === needCtrl &&
          e.altKey === needAlt &&
          e.shiftKey === needShift
        ) {
          e.preventDefault();
          e.stopPropagation();
          s.action();
          return;
        }
      }
    };

    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [shortcuts]);
}

/**
 * Pre-built navigation shortcuts used across the dashboard.
 * Call this inside any component within a Router context.
 */
export function useGlobalShortcuts(opts?: {
  onSearch?: () => void;
  onCreate?: () => void;
}) {
  const navigate = useNavigate();

  useKeyboardShortcuts([
    { combo: 'ctrl+k', label: 'Focus search', action: () => opts?.onSearch?.() },
    { combo: 'ctrl+n', label: 'Create new', action: () => opts?.onCreate?.() },
    { combo: 'alt+d', label: 'Go to Dashboard', action: () => navigate('/') },
    { combo: 'alt+s', label: 'Go to Sales', action: () => navigate('/sales') },
    { combo: 'alt+i', label: 'Go to Inventory', action: () => navigate('/inventory') },
    { combo: 'alt+c', label: 'Go to Customers', action: () => navigate('/customers') },
    { combo: 'alt+e', label: 'Go to Employees', action: () => navigate('/employees') },
    { combo: 'escape', label: 'Close modal', action: () => {} }, // handled per-page
  ]);
}
