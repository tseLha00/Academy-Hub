import { ChevronDown, Languages } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { LanguageSwitcher } from '../molecules/LanguageSwitcher'
import './organisms.css'

export function UserMenu({ userName }: { userName: string }) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return

    function close(event: MouseEvent | KeyboardEvent) {
      const clickedOutside =
        event instanceof MouseEvent && !menuRef.current?.contains(event.target as Node)
      const pressedEscape = event instanceof KeyboardEvent && event.key === 'Escape'
      if (clickedOutside || pressedEscape) setOpen(false)
    }

    document.addEventListener('mousedown', close)
    document.addEventListener('keydown', close)
    return () => {
      document.removeEventListener('mousedown', close)
      document.removeEventListener('keydown', close)
    }
  }, [open])

  return (
    <div className="user-menu" ref={menuRef}>
      <button
        type="button"
        className="user-menu-trigger"
        aria-expanded={open}
        onClick={() => setOpen(!open)}
      >
        <span className="user-menu-avatar" aria-hidden="true">
          {userName.charAt(0)}
        </span>
        {userName}
        <ChevronDown size={16} aria-hidden="true" />
      </button>

      {open && (
        <div className="user-menu-panel" role="dialog" aria-label={t('topbar.profileMenu')}>
          <span className="user-menu-label">
            <Languages size={16} aria-hidden="true" />
            {t('language.label')}
          </span>
          <LanguageSwitcher />
        </div>
      )}
    </div>
  )
}
