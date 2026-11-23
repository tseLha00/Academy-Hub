import { useTranslation } from 'react-i18next'
import { SUPPORTED_LANGUAGES } from '../../i18n'
import './molecules.css'

export function LanguageSwitcher() {
  const { t, i18n } = useTranslation()

  return (
    <div className="language-switcher" role="group" aria-label={t('language.label')}>
      {SUPPORTED_LANGUAGES.map((language) => (
        <button
          key={language}
          type="button"
          aria-pressed={i18n.resolvedLanguage === language}
          onClick={() => void i18n.changeLanguage(language)}
        >
          {language.toUpperCase()}
        </button>
      ))}
    </div>
  )
}
