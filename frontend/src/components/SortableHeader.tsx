import { ArrowUpDown, ArrowUp, ArrowDown } from 'lucide-react';

// Export as a const so bundlers don't strip it (avoids "not exported" Rollup error)
export type SortDir = 'asc' | 'desc' | '';

interface SortableHeaderProps {
  label: string;
  field: string;
  currentSort: string;
  currentDir: SortDir;
  onSort: (field: string, dir: SortDir) => void;
}

/**
 * Clickable table header cell that toggles sorting:
 *   none → asc → desc → none
 */
export default function SortableHeader({ label, field, currentSort, currentDir, onSort }: SortableHeaderProps) {
  const isActive = currentSort === field;

  const cycle = () => {
    if (!isActive || currentDir === '') onSort(field, 'asc');
    else if (currentDir === 'asc') onSort(field, 'desc');
    else onSort(field, '');
  };

  return (
    <th
      onClick={cycle}
      className="px-5 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer select-none hover:bg-gray-100 transition-colors group"
    >
      <div className="flex items-center gap-1.5">
        <span>{label}</span>
        <span className="inline-flex text-gray-300 group-hover:text-gray-500 transition-colors">
          {isActive && currentDir === 'asc' && <ArrowUp className="h-3.5 w-3.5 text-blue-600" />}
          {isActive && currentDir === 'desc' && <ArrowDown className="h-3.5 w-3.5 text-blue-600" />}
          {(!isActive || currentDir === '') && <ArrowUpDown className="h-3.5 w-3.5" />}
        </span>
      </div>
    </th>
  );
}
