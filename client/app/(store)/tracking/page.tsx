"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Search } from "lucide-react";
import clientAxios from '@/lib/axios/clientAxios';

export default function TrackingPage() {
  const [token, setToken] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();

  async function handleTrack(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (!token.trim()) {
      setError("Vui lòng nhập mã tra cứu");
      return;
    }

    setLoading(true);
    try {
      await clientAxios.get(`/api/public/orders/track/${token.trim()}`);
      router.push(`/tracking/${token.trim()}`);
    } catch (err: any) {
      setError("Mã tra cứu không hợp lệ hoặc đơn hàng không tồn tại");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-[60vh] bg-paper flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white p-8 border border-line rounded-sm shadow-sm text-center">
        <h1 className="text-2xl font-bold text-ink uppercase tracking-wider mb-2">
          Tra cứu đơn hàng
        </h1>
        <p className="text-muted text-sm mb-8">
          Vui lòng nhập mã tra cứu được gửi trong email xác nhận để xem tình trạng đơn hàng của bạn.
        </p>

        <form onSubmit={handleTrack} className="space-y-4">
          <div>
            <input
              type="text"
              value={token}
              onChange={(e) => {
                setToken(e.target.value);
                if (error) setError("");
              }}
              placeholder="Nhập mã tra cứu (VD: 550e8400-...)"
              className={`w-full px-4 py-3 border rounded-sm text-sm text-ink placeholder:text-faint focus:outline-none transition-colors ${
                error ? "border-danger focus:border-danger" : "border-line focus:border-ink"
              }`}
            />
            {error && <p className="text-danger text-[12px] text-left mt-2">{error}</p>}
          </div>
          <button
            type="submit"
            disabled={loading || !token.trim()}
            className="w-full flex items-center justify-center gap-2 bg-ink text-white py-3 rounded-sm font-bold uppercase tracking-wider text-sm hover:bg-accent transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <Search size={18} />
                Tra cứu
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
