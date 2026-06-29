'use client';

import { useState, useEffect, useRef } from 'react';
import axios from 'axios';

export interface Locality {
  id: string;
  name: string;
}



interface AddressSelectorProps {
  province: string;
  provinceCode?: string;
  ward: string;
  wardCode?: string;
  onChange: (data: { province: string; provinceCode: string; ward: string; wardCode: string }) => void;
  provinceError?: string;
  wardError?: string;
}

export default function AddressSelector({
  province,
  provinceCode,
  ward,
  wardCode,
  onChange,
  provinceError,
  wardError,
}: AddressSelectorProps) {
  const [provinces, setProvinces] = useState<Locality[]>([]);
  const [wards, setWards] = useState<Locality[]>([]);
  const [selectedProvinceId, setSelectedProvinceId] = useState<string>('');
  
  const [loadingProvinces, setLoadingProvinces] = useState(false);
  const [loadingWards, setLoadingWards] = useState(false);

  const [provSearch, setProvSearch] = useState('');
  const [wardSearch, setWardSearch] = useState('');
  
  const [provOpen, setProvOpen] = useState(false);
  const [wardOpen, setWardOpen] = useState(false);

  const provRef = useRef<HTMLDivElement>(null);
  const wardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (provRef.current && !provRef.current.contains(e.target as Node)) {
        setProvOpen(false);
      }
      if (wardRef.current && !wardRef.current.contains(e.target as Node)) {
        setWardOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    async function fetchProvinces() {
      setLoadingProvinces(true);
      try {
        const res = await axios.get('/api/shipping/provinces');
        if (res.data?.data) {
          setProvinces(res.data.data);
        }
      } catch (err) {
        console.error('Failed to fetch provinces:', err);
      } finally {
        setLoadingProvinces(false);
      }
    }
    fetchProvinces();
  }, []);

  useEffect(() => {
    if (province && provinces.length > 0 && !selectedProvinceId) {
      const match = provinces.find(p => p.name.toLowerCase() === province.toLowerCase());
      if (match) {
        setSelectedProvinceId(match.id);
      }
    }
  }, [province, provinces, selectedProvinceId]);

  useEffect(() => {
    if (!selectedProvinceId) {
      setWards([]);
      return;
    }
    async function fetchWards() {
      setLoadingWards(true);
      try {
        const res = await axios.get(`/api/shipping/wards?provinceId=${selectedProvinceId}`);
        if (res.data?.data) {
          setWards(res.data.data);
        }
      } catch (err) {
        console.error('Failed to fetch wards:', err);
      } finally {
        setLoadingWards(false);
      }
    }
    fetchWards();
  }, [selectedProvinceId]);

  const filteredProvinces = provinces.filter(p =>
    p.name.toLowerCase().includes(provSearch.toLowerCase())
  );

  const filteredWards = wards.filter(w =>
    w.name.toLowerCase().includes(wardSearch.toLowerCase())
  );

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
      {/* Province Combobox */}
      <div className="relative" ref={provRef}>
        <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
          Tỉnh / Thành phố <span className="text-danger">*</span>
        </label>
        <div
          onClick={() => setProvOpen(!provOpen)}
          className={`w-full border rounded px-3 py-2.5 text-sm bg-white cursor-pointer flex items-center justify-between transition-colors ${
            provinceError ? 'border-danger' : 'border-line hover:border-ink'
          }`}
        >
          <span className={province ? 'text-ink font-medium truncate' : 'text-faint truncate'}>
            {loadingProvinces ? 'Đang tải...' : (province || 'Chọn Tỉnh / Thành phố')}
          </span>
          <svg className={`w-4 h-4 text-muted flex-shrink-0 ml-2 transition-transform ${provOpen ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
        {provinceError && <p className="text-xs text-danger mt-1">{provinceError}</p>}

        {provOpen && (
          <div className="absolute z-50 mt-1 w-full bg-white border border-line rounded shadow-lg max-h-60 overflow-hidden flex flex-col">
            <div className="p-2 border-b border-line bg-line-2/50">
              <input
                type="text"
                placeholder="Tìm Tỉnh/Thành phố..."
                value={provSearch}
                onChange={e => setProvSearch(e.target.value)}
                onClick={e => e.stopPropagation()}
                className="w-full px-2.5 py-1.5 text-xs border border-line rounded focus:outline-none focus:border-ink bg-white"
                autoFocus
              />
            </div>
            <div className="overflow-y-auto flex-1 p-1">
              {filteredProvinces.length === 0 ? (
                <div className="p-3 text-xs text-center text-muted">Không tìm thấy địa phương</div>
              ) : (
                filteredProvinces.map(p => (
                  <div
                    key={p.id}
                    onClick={() => {
                      setSelectedProvinceId(p.id);
                      setProvOpen(false);
                      setProvSearch('');
                      onChange({ province: p.name, provinceCode: p.id, ward: '', wardCode: '' });
                    }}
                    className={`px-3 py-2 text-xs rounded cursor-pointer transition-colors ${
                      province === p.name ? 'bg-accent text-white font-semibold' : 'hover:bg-line-2 text-ink'
                    }`}
                  >
                    {p.name}
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>

      {/* Ward Combobox */}
      <div className="relative" ref={wardRef}>
        <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
          Phường / Xã <span className="text-danger">*</span>
        </label>
        <div
          onClick={() => {
            if (!province) return;
            setWardOpen(!wardOpen);
          }}
          className={`w-full border rounded px-3 py-2.5 text-sm bg-white flex items-center justify-between transition-colors ${
            !province ? 'bg-line-2 cursor-not-allowed opacity-60 border-line' : 'cursor-pointer hover:border-ink border-line'
          } ${wardError ? 'border-danger' : ''}`}
        >
          <span className={ward ? 'text-ink font-medium truncate' : 'text-faint truncate'}>
            {!province ? 'Vui lòng chọn Tỉnh trước' : loadingWards ? 'Đang tải...' : (ward || 'Chọn Phường / Xã')}
          </span>
          <svg className={`w-4 h-4 text-muted flex-shrink-0 ml-2 transition-transform ${wardOpen ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
        {wardError && <p className="text-xs text-danger mt-1">{wardError}</p>}

        {wardOpen && province && (
          <div className="absolute z-50 mt-1 w-full bg-white border border-line rounded shadow-lg max-h-60 overflow-hidden flex flex-col">
            <div className="p-2 border-b border-line bg-line-2/50">
              <input
                type="text"
                placeholder="Tìm Phường/Xã..."
                value={wardSearch}
                onChange={e => setWardSearch(e.target.value)}
                onClick={e => e.stopPropagation()}
                className="w-full px-2.5 py-1.5 text-xs border border-line rounded focus:outline-none focus:border-ink bg-white"
                autoFocus
              />
            </div>
            <div className="overflow-y-auto flex-1 p-1">
              {filteredWards.length === 0 ? (
                <div className="p-3 text-xs text-center text-muted">Không tìm thấy địa phương</div>
              ) : (
                filteredWards.map(w => (
                  <div
                    key={w.id}
                    onClick={() => {
                      setWardOpen(false);
                      setWardSearch('');
                      onChange({ province, provinceCode: selectedProvinceId, ward: w.name, wardCode: w.id });
                    }}
                    className={`px-3 py-2 text-xs rounded cursor-pointer transition-colors ${
                      ward === w.name ? 'bg-accent text-white font-semibold' : 'hover:bg-line-2 text-ink'
                    }`}
                  >
                    {w.name}
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
