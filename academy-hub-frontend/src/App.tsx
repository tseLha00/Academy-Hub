import { Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/templates/AppLayout'
import { PlaceholderPage } from './pages/PlaceholderPage'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="*" element={<PlaceholderPage />} />
      </Route>
    </Routes>
  )
}

export default App
