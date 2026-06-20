export default function PhoneField({
  label,
  value,
  onChange,
  error,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
}) {
  return (
    <div>
      <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">{label}</label>
      <div className="flex relative">
        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-ink z-10 font-medium">+84</span>
        <input
          type="tel"
          value={value.startsWith('+84') ? value.slice(3) : value}
          onChange={e => {
            const val = e.target.value.replace(/\D/g, '');
            onChange(val ? `+84${val}` : '');
          }}
          placeholder="912345678"
          className="w-full border border-line rounded-sm pl-10 pr-3 py-2 text-sm focus:outline-none focus:border-ink"
        />
      </div>
      {error && <p className="text-danger text-xs mt-1">{error}</p>}
    </div>
  );
}
