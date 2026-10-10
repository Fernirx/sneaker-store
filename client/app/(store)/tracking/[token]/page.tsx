"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import clientAxios from '@/lib/axios/clientAxios';
import OrderDetailClient from "../../orders/[id]/_components/OrderDetailClient";
import OrderDetailSkeleton from "../../orders/[id]/_components/OrderDetailSkeleton";
import Link from "next/link";
import { OrderResponse, OrderStatusHistoryResponse } from "../../orders/_components/types";

export default function TrackOrderDetailPage() {
  const { token } = useParams();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [history, setHistory] = useState<OrderStatusHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    Promise.all([
      clientAxios.get(`/api/public/orders/track/${token}`),
      clientAxios.get(`/api/public/orders/track/${token}/history`),
    ])
      .then(([orderRes, historyRes]) => {
        setOrder(orderRes.data.data);
        setHistory(historyRes.data.data ?? []);
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [token]);

  if (loading) return <OrderDetailSkeleton />;

  if (error || !order) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center">
        <h1 className="text-2xl font-bold mb-4">Không tìm thấy đơn hàng</h1>
        <p className="text-muted mb-8">Mã tra cứu không hợp lệ hoặc đơn hàng không tồn tại.</p>
        <Link href="/tracking" className="bg-ink text-white px-6 py-3 font-bold uppercase tracking-wider text-sm rounded-sm hover:bg-accent transition-colors">
          Thử lại
        </Link>
      </div>
    );
  }

  return <OrderDetailClient orderId={order.id.toString()} initialOrder={order} initialHistory={history} isTrackingMode={true} />;
}
