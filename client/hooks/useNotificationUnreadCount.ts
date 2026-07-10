'use client';

import { useCallback, useEffect, useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';

/**
 * basePath: '/api/notifications' (khách hàng) hoặc '/api/admin/notifications' (nhân viên).
 * Mở EventSource tới `${basePath}/stream` (proxy qua Next.js Route Handler, giữ nguyên BFF) -
 * mỗi khi có thông báo mới đẩy về thì tăng badge ngay; lúc mở/kết nối lại luôn refetch số thật
 * từ server để tránh lệch nếu bỏ lỡ event trong lúc mất kết nối.
 */
export function useNotificationUnreadCount(basePath: string) {
  const [count, setCount] = useState(0);

  const refetch = useCallback(() => {
    clientAxios.get(`${basePath}/unread-count`)
      .then(({ data }) => setCount(typeof data?.data === 'number' ? data.data : 0))
      .catch(() => {});
  }, [basePath]);

  useEffect(() => {
    refetch();

    const source = new EventSource(`${basePath}/stream`);
    source.addEventListener('notification', () => setCount(c => c + 1));
    source.onopen = refetch;

    return () => source.close();
  }, [basePath, refetch]);

  return count;
}
