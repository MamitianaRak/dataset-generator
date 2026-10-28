import type { ReactNode } from 'react';
import { Icon } from './Icon';

interface BannerProps {
  kind: 'error' | 'warning' | 'success';
  onClose: () => void;
  children: ReactNode;
}

export function Banner({ kind, onClose, children }: BannerProps) {
  return (
    <div className={`banner banner-${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      <span className="banner-dot" />
      <span className="banner-text">{children}</span>
      <button className="banner-close" onClick={onClose} aria-label="Fermer"><Icon name="x" size={14} /></button>
    </div>
  );
}
