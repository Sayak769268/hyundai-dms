import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import { Keyboard, X } from 'lucide-react';

interface ShortcutItem {
  keys: string[];
  desc: string;
}
interface ShortcutGroup {
  title: string;
  items: ShortcutItem[];
}

const GROUPS: ShortcutGroup[] = [
  {
    title: 'Navigation',
    items: [
      { keys: ['Alt', 'D'], desc: 'Dashboard' },
      { keys: ['Alt', 'S'], desc: 'Sales' },
      { keys: ['Alt', 'I'], desc: 'Inventory' },
      { keys: ['Alt', 'C'], desc: 'Customers' },
      { keys: ['Alt', 'E'], desc: 'Employees' },
    ],
  },
  {
    title: 'Actions',
    items: [
      { keys: ['Ctrl', 'K'], desc: 'Focus search bar' },
      { keys: ['Ctrl', 'N'], desc: 'Create new record' },
      { keys: ['?'], desc: 'Open this shortcuts panel' },
      { keys: ['Esc'], desc: 'Close modal / dialog' },
    ],
  },
];

function Key({ k }: { k: string }) {
  return (
    <kbd className="inline-flex items-center justify-center min-w-[28px] h-7 px-2 bg-gray-100 border border-gray-300 rounded-md text-[11px] font-semibold text-gray-700 shadow-sm font-mono">
      {k}
    </kbd>
  );
}

export default function KeyboardShortcutsModal() {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      // Don't trigger inside inputs / textareas / selects
      const tag = (e.target as HTMLElement).tagName;
      if (['INPUT', 'TEXTAREA', 'SELECT'].includes(tag)) return;

      if (e.key === '?' && !e.ctrlKey && !e.altKey && !e.metaKey) {
        e.preventDefault();
        setOpen(true);
      }
      if (e.key === 'Escape') {
        setOpen(false);
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);

  if (!open) return null;

  return createPortal(
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center p-4"
      style={{ backdropFilter: 'blur(4px)', backgroundColor: 'rgba(0,0,0,0.45)' }}
      onClick={() => setOpen(false)}
    >
      <div
        className="bg-white rounded-2xl shadow-2xl w-full max-w-lg flex flex-col max-h-[85vh] overflow-hidden"
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 bg-blue-50 rounded-lg flex items-center justify-center">
              <Keyboard className="h-4 w-4 text-blue-600" />
            </div>
            <h2 className="text-base font-bold text-gray-900">Keyboard Shortcuts</h2>
          </div>
          <button
            onClick={() => setOpen(false)}
            className="p-1.5 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Body */}
        <div className="px-6 py-5 space-y-5 overflow-y-auto flex-1">
          {GROUPS.map(group => (
            <div key={group.title}>
              <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2.5">
                {group.title}
              </p>
              <div className="space-y-1.5">
                {group.items.map(item => (
                  <div
                    key={item.desc}
                    className="flex items-center justify-between py-2 px-3 rounded-lg hover:bg-gray-50 transition"
                  >
                    <span className="text-sm text-gray-700">{item.desc}</span>
                    <div className="flex items-center gap-1">
                      {item.keys.map((k, i) => (
                        <span key={k} className="flex items-center gap-1">
                          <Key k={k} />
                          {i < item.keys.length - 1 && (
                            <span className="text-[10px] text-gray-400 font-medium">+</span>
                          )}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Footer hint */}
        <div className="px-6 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between">
          <span className="text-xs text-gray-400">Press</span>
          <div className="flex items-center gap-1.5">
            <Key k="?" />
            <span className="text-xs text-gray-400">to toggle</span>
            <span className="mx-1.5 text-gray-300">·</span>
            <Key k="Esc" />
            <span className="text-xs text-gray-400">to close</span>
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}
