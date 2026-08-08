export default function Modal({
  title,
  onClose,
  children,
  maxWidth = 'md',
}: {
  title: string;
  onClose: () => void;
  children: React.ReactNode;
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl' | '5xl';
}) {
  const maxWidthClass = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
    '2xl': 'max-w-2xl',
    '3xl': 'max-w-3xl',
    '4xl': 'max-w-4xl',
    '5xl': 'max-w-5xl',
  }[maxWidth];

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 py-6"
      onClick={onClose}
    >
      <div
        className={`bg-white rounded-lg shadow-xl w-full ${maxWidthClass} mx-4 flex flex-col max-h-full`}
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-line shrink-0">
          <h2 className="font-display font-black text-sm uppercase tracking-wide">{title}</h2>
          <button
            onClick={onClose}
            className="text-muted hover:text-ink text-xl leading-none transition-colors"
          >
            &times;
          </button>
        </div>
        <div className="px-5 py-4 overflow-y-auto">{children}</div>
      </div>
    </div>
  );
}
