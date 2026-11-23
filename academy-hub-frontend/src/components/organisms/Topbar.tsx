import { Bell } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import logo from '../images/Logo.jpg'
import { SearchField } from '../molecules/SearchField'
import { UserMenu } from './UserMenu'
import './organisms.css'

export function Topbar() {
  const { t } = useTranslation()

  return (
    <header className="topbar">
      <div className="topbar-brand">
        <img className="topbar-logo" src={logo} alt={t('topbar.logo')} />
      </div>
      <span className="topbar-title">Academy Hub</span>
      <SearchField />
      <div className="topbar-actions">
        <button type="button" className="topbar-bell" aria-label={t('topbar.notifications')}>
          <Bell size={20} />
        </button>
        {/* TODO: use the logged-in user from GET /api/auth/me. */}
        <UserMenu userName="Admin" />
      </div>
    </header>
  )
}
