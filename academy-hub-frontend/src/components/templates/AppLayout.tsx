import { Outlet } from 'react-router-dom'
import { Sidebar } from '../organisms/Sidebar'
import { Topbar } from '../organisms/Topbar'
import './templates.css'

export function AppLayout() {
  return (
    <div className="app-layout">
      <Topbar />
      <Sidebar />
      <main className="app-layout-content">
        <Outlet />
      </main>
    </div>
  )
}
