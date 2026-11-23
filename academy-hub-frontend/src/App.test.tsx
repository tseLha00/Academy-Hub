import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import App from './App'

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  )
}

describe('App layout', () => {
  it('highlights the sidebar entry of the current route', () => {
    renderAt('/trainings')
    const nav = screen.getByRole('navigation', { name: 'Hauptnavigation' })
    expect(within(nav).getByRole('link', { name: 'Trainings' })).toHaveClass('active')
    expect(within(nav).getByRole('link', { name: 'Lernende' })).not.toHaveClass('active')
    expect(screen.getByText('Diese Seite wurde noch nicht erstellt.')).toBeInTheDocument()
  })

  it('switches between German and English in the profile menu', async () => {
    const user = userEvent.setup()
    renderAt('/')

    await user.click(screen.getByRole('button', { name: /Admin/ }))
    await user.click(screen.getByRole('button', { name: 'EN' }))

    expect(screen.getByRole('link', { name: 'Learners' })).toBeInTheDocument()
    expect(document.documentElement.lang).toBe('en')
    expect(localStorage.getItem('academy-hub-language')).toBe('en')
  })
})
