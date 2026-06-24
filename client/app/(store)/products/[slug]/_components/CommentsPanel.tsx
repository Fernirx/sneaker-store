'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { avatarUrl } from '@/lib/cloudinaryUrl';
import { formatReviewDate, type CommentItem, type PageMeta } from './reviewTypes';

function Avatar({ name, publicId }: { name: string; publicId: string | null }) {
  if (publicId) {
    return <img src={avatarUrl(publicId, 56)} alt={name} className="w-8 h-8 rounded-full object-cover ring-1 ring-line shrink-0" />;
  }
  return (
    <div className="w-8 h-8 rounded-full bg-accent/10 flex items-center justify-center shrink-0">
      <span className="font-display font-black text-xs text-accent">{name.charAt(0).toUpperCase()}</span>
    </div>
  );
}

function insertReply(nodes: CommentItem[], parentId: number, newNode: CommentItem): CommentItem[] {
  return nodes.map(n => {
    if (n.id === parentId) return { ...n, replies: [...n.replies, newNode] };
    if (n.replies.length) return { ...n, replies: insertReply(n.replies, parentId, newNode) };
    return n;
  });
}

function updateContent(nodes: CommentItem[], id: number, content: string, updatedAt: string): CommentItem[] {
  return nodes.map(n => n.id === id
    ? { ...n, content, updatedAt }
    : { ...n, replies: updateContent(n.replies, id, content, updatedAt) });
}

function removeNode(nodes: CommentItem[], id: number): CommentItem[] {
  return nodes.filter(n => n.id !== id).map(n => ({ ...n, replies: removeNode(n.replies, id) }));
}

function CommentNode({
  comment,
  depth,
  isLoggedIn,
  currentUserId,
  onReply,
  onEdit,
  onRequestDelete,
}: {
  comment: CommentItem;
  depth: number;
  isLoggedIn: boolean;
  currentUserId: number | null;
  onReply: (parentId: number, content: string) => Promise<void>;
  onEdit: (id: number, content: string) => Promise<void>;
  onRequestDelete: (comment: CommentItem) => void;
}) {
  const isOwner = comment.user.userId === currentUserId;

  const [replyOpen, setReplyOpen]   = useState(false);
  const [replyText, setReplyText]   = useState('');
  const [replyPending, setReplyPending] = useState(false);
  const [replyError, setReplyError] = useState('');

  const [editOpen, setEditOpen]     = useState(false);
  const [editText, setEditText]     = useState(comment.content);
  const [editPending, setEditPending] = useState(false);
  const [editError, setEditError]   = useState('');

  async function submitReply() {
    if (!replyText.trim()) return;
    setReplyPending(true);
    setReplyError('');
    try {
      await onReply(comment.id, replyText.trim());
      setReplyText('');
      setReplyOpen(false);
    } catch (err) {
      setReplyError(parseApiError(err, "Không thể gửi trả lời").general);
    } finally {
      setReplyPending(false);
    }
  }

  async function submitEdit() {
    if (!editText.trim()) return;
    setEditPending(true);
    setEditError('');
    try {
      await onEdit(comment.id, editText.trim());
      setEditOpen(false);
    } catch (err) {
      setEditError(parseApiError(err, "Không thể lưu thay đổi").general);
    } finally {
      setEditPending(false);
    }
  }

  const indent = Math.min(depth, 4) * 28;

  return (
    <div style={{ marginLeft: depth > 0 ? indent : 0 }} className={depth > 0 ? 'border-l border-line pl-4 mt-3' : ''}>
      <div className="flex items-start gap-3">
        <Avatar name={comment.user.displayName} publicId={comment.user.avatarPublicId} />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <p className="font-semibold text-sm">{comment.user.displayName}</p>
            <span className="text-xs text-muted">{formatReviewDate(comment.createdAt)}</span>
          </div>

          {editOpen ? (
            <div className="mt-1.5 space-y-2">
              {editError && <p className="text-xs text-danger">{editError}</p>}
              <textarea
                value={editText}
                onChange={e => setEditText(e.target.value)}
                rows={2}
                className="w-full border border-line rounded px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
              />
              <div className="flex gap-2">
                <button onClick={submitEdit} disabled={editPending}
                  className="px-3 py-1.5 bg-accent text-white text-xs font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60">
                  {editPending ? "Đang lưu..." : "Lưu"}
                </button>
                <button onClick={() => { setEditOpen(false); setEditText(comment.content); }}
                  className="px-3 py-1.5 border border-line text-xs font-bold rounded-sm hover:bg-paper">
                  {"Hủy"}
                </button>
              </div>
            </div>
          ) : (
            <p className="text-sm text-ink-2 mt-1 whitespace-pre-line">{comment.content}</p>
          )}

          {!editOpen && (
            <div className="flex gap-3 mt-1.5">
              {isLoggedIn && (
                <button onClick={() => setReplyOpen(o => !o)} className="text-xs font-bold text-muted hover:text-ink transition-colors">
                  {"Trả lời"}
                </button>
              )}
              {isOwner && (
                <>
                  <button onClick={() => setEditOpen(true)} className="text-xs font-bold text-muted hover:text-ink transition-colors">
                    {"Sửa"}
                  </button>
                  <button onClick={() => onRequestDelete(comment)} className="text-xs font-bold text-danger hover:opacity-75 transition-opacity">
                    {"Xóa"}
                  </button>
                </>
              )}
            </div>
          )}

          {replyOpen && (
            <div className="mt-2 space-y-2">
              {replyError && <p className="text-xs text-danger">{replyError}</p>}
              <textarea
                value={replyText}
                onChange={e => setReplyText(e.target.value)}
                rows={2}
                placeholder={"Viết trả lời..."}
                className="w-full border border-line rounded px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
              />
              <div className="flex gap-2">
                <button onClick={submitReply} disabled={replyPending}
                  className="px-3 py-1.5 bg-accent text-white text-xs font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60">
                  {replyPending ? "Đang gửi..." : "Gửi"}
                </button>
                <button onClick={() => { setReplyOpen(false); setReplyText(''); }}
                  className="px-3 py-1.5 border border-line text-xs font-bold rounded-sm hover:bg-paper">
                  {"Hủy"}
                </button>
              </div>
            </div>
          )}

          {comment.replies.map(child => (
            <CommentNode
              key={child.id}
              comment={child}
              depth={depth + 1}
              isLoggedIn={isLoggedIn}
              currentUserId={currentUserId}
              onReply={onReply}
              onEdit={onEdit}
              onRequestDelete={onRequestDelete}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

export default function CommentsPanel({
  slug,
  productId,
  isLoggedIn,
  currentUserId,
}: {
  slug: string;
  productId: number;
  isLoggedIn: boolean;
  currentUserId: number | null;
}) {
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [meta, setMeta]         = useState<PageMeta | null>(null);
  const [page, setPage]         = useState(0);
  const [loading, setLoading]   = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const [newContent, setNewContent] = useState('');
  const [posting, setPosting]       = useState(false);
  const [postError, setPostError]   = useState('');

  const [deleteTarget, setDeleteTarget] = useState<CommentItem | null>(null);
  const [deleting, setDeleting]         = useState(false);
  const [deleteError, setDeleteError]   = useState('');

  async function fetchComments(pageNum: number, append: boolean) {
    if (append) setLoadingMore(true); else setLoading(true);
    try {
      const { data } = await clientAxios.get(`/api/products/${slug}/comments?page=${pageNum}&size=10`);
      setComments(prev => append ? [...prev, ...data.data] : data.data);
      setMeta(data.meta as PageMeta);
      setPage(pageNum);
    } finally {
      if (append) setLoadingMore(false); else setLoading(false);
    }
  }

  useEffect(() => {
    fetchComments(0, false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [slug]);

  async function handlePostRoot() {
    if (!newContent.trim()) return;
    setPosting(true);
    setPostError('');
    try {
      const { data } = await clientAxios.post('/api/me/comments', { productId, content: newContent.trim() });
      setComments(prev => [...prev, data.data as CommentItem]);
      setNewContent('');
    } catch (err) {
      setPostError(parseApiError(err, "Không thể gửi bình luận").general);
    } finally {
      setPosting(false);
    }
  }

  async function handleReply(parentId: number, content: string) {
    const { data } = await clientAxios.post('/api/me/comments', { productId, parentId, content });
    setComments(prev => insertReply(prev, parentId, data.data as CommentItem));
  }

  async function handleEdit(id: number, content: string) {
    const { data } = await clientAxios.patch(`/api/me/comments/${id}`, { content });
    const updated = data.data as CommentItem;
    setComments(prev => updateContent(prev, id, updated.content, updated.updatedAt));
  }

  async function handleConfirmDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await clientAxios.delete(`/api/me/comments/${deleteTarget.id}`);
      setComments(prev => removeNode(prev, deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      setDeleteError(parseApiError(err, "Không thể xóa bình luận").general);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="max-w-3xl">
      {isLoggedIn ? (
        <div className="space-y-2 mb-8">
          {postError && <p className="text-sm text-danger">{postError}</p>}
          <textarea
            value={newContent}
            onChange={e => setNewContent(e.target.value)}
            rows={3}
            placeholder={"Đặt câu hỏi hoặc chia sẻ ý kiến về sản phẩm..."}
            className="w-full border-[1.5px] border-line rounded-sm px-3.5 py-3 text-sm focus:outline-none focus:border-ink transition-colors resize-none"
          />
          <button
            onClick={handlePostRoot}
            disabled={posting || !newContent.trim()}
            className="px-5 py-2.5 bg-ink text-white font-display font-bold text-[11px] uppercase tracking-wider rounded-sm hover:bg-accent transition-colors disabled:opacity-40"
          >
            {posting ? "Đang gửi..." : "Gửi bình luận"}
          </button>
        </div>
      ) : (
        <div className="mb-8 py-4 px-4 border-[1.5px] border-line rounded-sm text-sm text-muted">
          <Link href="/login" className="font-semibold text-ink underline">{"Đăng nhập"}</Link>
          {" để đặt câu hỏi hoặc bình luận."}
        </div>
      )}

      {loading ? (
        <p className="text-sm text-muted py-16 text-center">{"Đang tải..."}</p>
      ) : comments.length === 0 ? (
        <div className="flex items-center justify-center py-16 text-sm text-muted border-[1.5px] border-line rounded-lg">
          {"Chưa có bình luận nào."}
        </div>
      ) : (
        <div className="space-y-5">
          {comments.map(c => (
            <CommentNode
              key={c.id}
              comment={c}
              depth={0}
              isLoggedIn={isLoggedIn}
              currentUserId={currentUserId}
              onReply={handleReply}
              onEdit={handleEdit}
              onRequestDelete={setDeleteTarget}
            />
          ))}

          {meta && !meta.last && (
            <button
              onClick={() => fetchComments(page + 1, true)}
              disabled={loadingMore}
              className="w-full py-3 border border-line rounded-sm text-sm font-bold hover:bg-paper transition-colors disabled:opacity-50"
            >
              {loadingMore ? "Đang tải..." : "Xem thêm"}
            </button>
          )}
        </div>
      )}

      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={() => setDeleteTarget(null)}>
          <div className="bg-white rounded-lg shadow-xl w-full max-w-sm p-5 space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="font-display font-black text-sm uppercase tracking-wide">{"Xóa bình luận"}</h3>
            {deleteError && <p className="text-danger text-sm">{deleteError}</p>}
            <p className="text-sm">{"Xóa bình luận này? Các trả lời bên dưới cũng sẽ bị xóa."}</p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">{"Hủy"}</button>
              <button onClick={handleConfirmDelete} disabled={deleting}
                className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60">
                {deleting ? "Đang xóa..." : "Xóa"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
