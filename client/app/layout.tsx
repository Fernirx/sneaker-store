import type { Metadata } from 'next';
import { Archivo, Hanken_Grotesk, JetBrains_Mono } from 'next/font/google';
import './globals.css';

const archivo = Archivo({
  subsets: ['latin', 'vietnamese'],
  weight: ['400', '600', '700', '800', '900'],
  variable: '--font-archivo',
  display: 'swap',
});

const hanken = Hanken_Grotesk({
  subsets: ['latin', 'vietnamese'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-hanken',
  display: 'swap',
});

const jetbrains = JetBrains_Mono({
  subsets: ['latin'],
  weight: ['400', '500', '700'],
  variable: '--font-jetbrains',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'STRIDE — Sneaker Store',
  description: 'Cửa hàng sneaker hiệu năng & lifestyle chính hãng',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html className={`${archivo.variable} ${hanken.variable} ${jetbrains.variable}`}>
      <body className="min-h-screen bg-paper font-body text-ink antialiased">
        {children}
      </body>
    </html>
  );
}
