export default function ImageUnavailable({ className = 'w-8 h-8' }: { className?: string }) {
  return (
    <div className="w-full h-full flex items-center justify-center text-faint" title="Hình ảnh không khả dụng">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
        className={className}
        aria-label="Hình ảnh không khả dụng"
      >
        <rect x="3" y="3" width="18" height="18" rx="2" />
        <circle cx="8.5" cy="8.5" r="1.5" />
        <path d="M21 15l-5-5L5 21" />
        <line x1="3" y1="3" x2="21" y2="21" />
      </svg>
    </div>
  );
}
