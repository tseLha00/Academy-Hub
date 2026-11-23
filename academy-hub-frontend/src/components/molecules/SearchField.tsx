import { Search } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import './molecules.css'

export function SearchField() {
  const { t } = useTranslation()

  return (
    <label className="search-field">
      <Search size={16} aria-hidden="true" />
      <input
        type="search"
        placeholder={t('topbar.searchPlaceholder')}
        aria-label={t('topbar.search')}
      />
    </label>
  )
}
