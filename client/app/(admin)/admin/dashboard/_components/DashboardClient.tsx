'use client';

import { useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import {
  type DashboardSummary,
  ORDER_STATUS_LABELS, ORDER_STATUS_CHART_COLOR, ORDER_STATUS_ORDER,
  formatPrice, formatCompactPrice, formatShortDate,
} from './types';
import {
  ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  BarChart, Bar, Cell, LabelList,
} from 'recharts';

function StatTile({ label, value, sub, href }: { label: string; value: string; sub?: string; href?: string }) {
  const content = (
    <div className="bg-paper border border-line rounded-sm p-4 h-full">
      <div className="text-[11px] font-bold uppercase tracking-wider text-muted mb-1">{label}</div>
      <div className="text-2xl font-black text-ink">{value}</div>
      {sub && <div className="text-xs text-muted mt-1">{sub}</div>}
    </div>
  );
  return href ? <Link href={href} className="block hover:opacity-80 transition-opacity">{content}</Link> : content;
}

function ChartCard({ title, headerRight, children }: { title: string; headerRight?: React.ReactNode; children: React.ReactNode }) {
  return (
    <div className="bg-paper border border-line rounded-sm p-4">
      <div className="flex items-center justify-between mb-4">
        <div className="text-[11px] font-bold uppercase tracking-wider text-muted">{title}</div>
        {headerRight && <div>{headerRight}</div>}
      </div>
      {children}
    </div>
  );
}

function RevenueTooltip({ active, payload, label }: { active?: boolean; payload?: { value: number }[]; label?: string }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-ink text-white text-xs rounded-sm px-3 py-2 shadow-lg">
      <div className="font-bold mb-0.5">{label ? formatShortDate(label) : ''}</div>
      <div>{formatPrice(payload[0].value)}</div>
    </div>
  );
}

function StatusTooltip({ active, payload }: { active?: boolean; payload?: { payload: { status: string; count: number } }[] }) {
  if (!active || !payload?.length) return null;
  const { status, count } = payload[0].payload;
  return (
    <div className="bg-ink text-white text-xs rounded-sm px-3 py-2 shadow-lg">
      <div className="font-bold mb-0.5">{ORDER_STATUS_LABELS[status] ?? status}</div>
      <div>{count} đơn</div>
    </div>
  );
}

export default function DashboardClient({ initialData, roles }: { initialData: DashboardSummary, roles: string[] }) {
  const [data, setData] = useState(initialData);
  const [refreshing, setRefreshing] = useState(false);
  const [topLimit, setTopLimit] = useState(5);
  const [revenueDays, setRevenueDays] = useState(14);

  async function refresh(limit = topLimit, days = revenueDays) {
    setRefreshing(true);
    try {
      const { data: res } = await clientAxios.get(`/api/admin/dashboard?topLimit=${limit}&revenueDays=${days}`);
      setData(res.data as DashboardSummary);
    } catch {
      // im lặng bỏ qua - dữ liệu cũ vẫn hiển thị, admin có thể bấm làm mới lại
    } finally {
      setRefreshing(false);
    }
  }

  function handleTopLimitChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const newLimit = parseInt(e.target.value, 10);
    setTopLimit(newLimit);
    refresh(newLimit, revenueDays);
  }

  function handleRevenueDaysChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const newDays = parseInt(e.target.value, 10);
    setRevenueDays(newDays);
    refresh(topLimit, newDays);
  }

  const statusData = ORDER_STATUS_ORDER.map(status => ({
    status,
    label: ORDER_STATUS_LABELS[status],
    count: data.orderCountByStatus[status] ?? 0,
  }));

  const stockAlertCount = data.lowStockCount + data.outOfStockCount;
  const isAdmin = roles.includes('ROLE_ADMIN');

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-2xl uppercase tracking-tight text-ink">Tổng quan</h1>
        <button
          type="button"
          onClick={refresh}
          disabled={refreshing}
          className="text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors disabled:opacity-50"
        >
          {refreshing ? 'Đang làm mới...' : 'Làm mới'}
        </button>
      </div>

      {/* Stat tiles */}
      <div className={`grid grid-cols-2 ${isAdmin ? 'md:grid-cols-4' : 'md:grid-cols-2'} gap-4`}>
        {isAdmin && (
          <>
            <StatTile label="Doanh thu hôm nay" value={formatPrice(data.revenueToday ?? 0)} />
            <StatTile label="Doanh thu tháng này" value={formatPrice(data.revenueThisMonth ?? 0)} />
          </>
        )}
        <StatTile label="Khách hàng mới (tháng)" value={String(data.newCustomersThisMonth)} href="/admin/customers" />
        <StatTile
          label="Cảnh báo tồn kho"
          value={String(stockAlertCount)}
          sub={`${data.outOfStockCount} hết hàng · ${data.lowStockCount} sắp hết`}
          href="/admin/products"
        />
      </div>

      {/* Charts */}
      <div className={`grid grid-cols-1 ${isAdmin ? 'lg:grid-cols-2' : 'lg:grid-cols-1'} gap-4`}>
        {isAdmin && data.revenueByDay && (
          <ChartCard
            title={`Doanh thu ${revenueDays} ngày gần nhất`}
            headerRight={
              <select
                value={revenueDays}
                onChange={handleRevenueDaysChange}
                disabled={refreshing}
                className="text-[11px] font-bold text-muted bg-transparent border-none focus:ring-0 cursor-pointer hover:text-ink outline-none"
              >
                <option value={7}>7 ngày</option>
                <option value={14}>14 ngày</option>
                <option value={30}>30 ngày</option>
              </select>
            }
          >
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={data.revenueByDay} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="revenueFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#ff3d14" stopOpacity={0.25} />
                    <stop offset="100%" stopColor="#ff3d14" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid vertical={false} stroke="#e2e8f0" />
                <XAxis
                  dataKey="date"
                  tickFormatter={formatShortDate}
                  tick={{ fontSize: 11, fill: '#64748b' }}
                  axisLine={{ stroke: '#e2e8f0' }}
                  tickLine={false}
                />
                <YAxis
                  tickFormatter={formatCompactPrice}
                  tick={{ fontSize: 11, fill: '#64748b' }}
                  axisLine={false}
                  tickLine={false}
                  width={48}
                />
                <Tooltip content={<RevenueTooltip />} />
                <Area type="monotone" dataKey="revenue" stroke="#ff3d14" strokeWidth={2} fill="url(#revenueFill)" />
              </AreaChart>
            </ResponsiveContainer>
          </ChartCard>
        )}

        <ChartCard title="Đơn hàng theo trạng thái">
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={statusData} layout="vertical" margin={{ top: 8, right: 24, left: 0, bottom: 0 }}>
              <CartesianGrid horizontal={false} stroke="#e2e8f0" />
              <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} />
              <YAxis
                type="category"
                dataKey="label"
                tick={{ fontSize: 11, fill: '#1e293b' }}
                axisLine={false}
                tickLine={false}
                width={90}
              />
              <Tooltip content={<StatusTooltip />} cursor={{ fill: '#f1f5f9' }} />
              <Bar dataKey="count" radius={[0, 4, 4, 0]} maxBarSize={22}>
                {statusData.map(entry => (
                  <Cell key={entry.status} fill={ORDER_STATUS_CHART_COLOR[entry.status]} />
                ))}
                <LabelList dataKey="count" position="right" style={{ fontSize: 11, fontWeight: 700, fill: '#0f172a' }} />
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      </div>

      {/* Tables */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="bg-paper border border-line rounded-sm p-4">
          <div className="flex items-center justify-between mb-3">
            <div className="text-[11px] font-bold uppercase tracking-wider text-muted">Sắp hết / hết hàng</div>
            <Link href="/admin/products" className="text-[11px] font-bold text-accent hover:underline">Xem tất cả</Link>
          </div>
          {data.lowStockVariants.length === 0 ? (
            <p className="text-sm text-muted">Không có biến thể nào sắp hết hàng.</p>
          ) : (
            <table className="w-full text-sm">
              <tbody>
                {data.lowStockVariants.map(v => (
                  <tr key={v.variantId} className="border-t border-line first:border-t-0">
                    <td className="py-2 pr-2">
                      <Link href={`/admin/products/${v.productId}`} className="font-bold text-ink hover:text-accent">
                        {v.productName}
                      </Link>
                      <div className="text-xs text-muted">{v.sku} · {v.colorway} · size {v.size}</div>
                    </td>
                    <td className="py-2 text-right whitespace-nowrap">
                      <span className={`font-black ${v.stockQuantity === 0 ? 'text-danger' : 'text-warn'}`}>
                        {v.stockQuantity}
                      </span>
                      <span className="text-xs text-muted"> / {v.minStockLevel}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="bg-paper border border-line rounded-sm p-4">
          <div className="flex items-center justify-between mb-3">
            <div className="text-[11px] font-bold uppercase tracking-wider text-muted">Sản phẩm bán chạy</div>
            <select
              value={topLimit}
              onChange={handleTopLimitChange}
              disabled={refreshing}
              className="text-[11px] font-bold text-muted bg-transparent border-none focus:ring-0 cursor-pointer hover:text-ink outline-none"
            >
              <option value={5}>Top 5</option>
              <option value={10}>Top 10</option>
              <option value={20}>Top 20</option>
            </select>
          </div>
          {data.topProducts.length === 0 ? (
            <p className="text-sm text-muted">Chưa có dữ liệu bán hàng.</p>
          ) : (
            <table className="w-full text-sm">
              <tbody>
                {data.topProducts.map(p => (
                  <tr key={p.productCode} className="border-t border-line first:border-t-0">
                    <td className="py-2 pr-2">
                      <div className="font-bold text-ink">{p.productName}</div>
                      <div className="text-xs text-muted">{p.productCode}</div>
                    </td>
                    <td className="py-2 text-right whitespace-nowrap font-black text-ink">{p.totalQuantity}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Pending actions */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <StatTile
          label="Phiếu nhập hàng chờ xử lý"
          value={String(data.pendingPurchases)}
          sub="Trạng thái DRAFT/CONFIRMED"
          href="/admin/purchases"
        />
        <StatTile
          label="Phiếu điều chỉnh kho chờ duyệt"
          value={String(data.pendingStockAdjustments)}
          sub="Trạng thái DRAFT"
          href="/admin/stock-adjustments"
        />
      </div>
    </div>
  );
}
