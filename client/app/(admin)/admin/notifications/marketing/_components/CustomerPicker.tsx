'use client';

import { useEffect, useRef, useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';

interface CustomerOption {
  id: number;
  email: string;
  firstName: string;
  lastName?: string;
}

export default function CustomerPicker({ selected, onChange }: {
  selected: CustomerOption[];
  onChange: (customers: CustomerOption[]) => void;
}) {
  const [query, setQuery] = useState('');
  const [options, setOptions] = useState<CustomerOption[]>([]);
  const [open, setOpen] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!query.trim()) { setOptions([]); return; }
    if (timer.current) clearTimeout(timer.current);
    timer.current = setTimeout(() => {
      clientAxios.get(`/api/admin/customers?page=0&size=10&search=${encodeURIComponent(query)}`)
        .then(({ data }) => setOptions(data.data ?? []))
        .catch(() => setOptions([]));
    }, 300);
  }, [query]);

  function addCustomer(c: CustomerOption) {
    if (!selected.some(s => s.id === c.id)) onChange([...selected, c]);
    setQuery('');
    setOptions([]);
    setOpen(false);
  }

  function removeCustomer(id: number) {
    onChange(selected.filter(s => s.id !== id));
  }

  return (
    <div>
      <div className="relative">
        <input
          type="text"
          value={query}
          onChange={e => { setQuery(e.target.value); setOpen(true); }}
          onFocus={() => setOpen(true)}
          placeholder="Tìm khách hàng theo email/tên..."
          className="w-full border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        />
        {open && options.length > 0 && (
          <div className="absolute z-10 mt-1 w-full bg-white border border-line rounded-sm shadow-lg max-h-56 overflow-y-auto">
            {options.map(c => (
              <button
                key={c.id}
                type="button"
                onClick={() => addCustomer(c)}
                className="w-full text-left px-3 py-2 text-sm hover:bg-paper transition-colors"
              >
                <span className="font-semibold">{[c.firstName, c.lastName].filter(Boolean).join(' ')}</span>
                <span className="text-muted ml-2 text-xs">{c.email}</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {selected.length > 0 && (
        <div className="flex flex-wrap gap-1.5 mt-2">
          {selected.map(c => (
            <span key={c.id} className="inline-flex items-center gap-1.5 bg-line-2 text-ink-2 text-xs px-2 py-1 rounded-sm">
              {c.email}
              <button type="button" onClick={() => removeCustomer(c.id)} className="text-muted hover:text-danger">×</button>
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
