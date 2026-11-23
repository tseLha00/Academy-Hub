import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import './molecules.css'

type NavItemProps = {
  label: string
  icon: LucideIcon
  to?: string
  small?: boolean
}

export function NavItem({ label, icon: Icon, to, small }: NavItemProps) {
  const className = small ? 'nav-item nav-item-small' : 'nav-item'
  const content = (
    <>
      <Icon size={small ? 16 : 18} aria-hidden="true" />
      {label}
    </>
  )

  return to ? (
    <NavLink to={to} className={className}>
      {content}
    </NavLink>
  ) : (
    <button type="button" className={className}>
      {content}
    </button>
  )
}
