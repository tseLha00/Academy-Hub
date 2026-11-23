import { ArrowLeftRight, Box, BriefcaseBusiness, CodeXml, House, LogOut, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { NavItem } from '../molecules/NavItem'
import './organisms.css'

const NAV_ITEMS = [
  { to: '/dashboard', labelKey: 'dashboard', icon: House },
  { to: '/trainings', labelKey: 'trainings', icon: CodeXml },
  { to: '/learners', labelKey: 'learners', icon: Users },
  { to: '/vacation-planner', labelKey: 'vacationPlanner', icon: BriefcaseBusiness },
  { to: '/learner-hub', labelKey: 'learnerHub', icon: Box },
] as const

export function Sidebar() {
  const { t } = useTranslation()

  return (
    <aside className="sidebar">
      <p className="sidebar-heading">{t('sidebar.heading')}</p>
      <nav aria-label={t('sidebar.navigation')} className="sidebar-group">
        {NAV_ITEMS.map(({ to, labelKey, icon }) => (
          <NavItem key={to} to={to} icon={icon} label={t(`sidebar.${labelKey}`)} />
        ))}
      </nav>

      <div className="sidebar-group sidebar-footer">
        <NavItem small icon={ArrowLeftRight} label={t('sidebar.switchToLearnerView')} />
        <NavItem icon={LogOut} label={t('sidebar.logout')} />
      </div>
    </aside>
  )
}
