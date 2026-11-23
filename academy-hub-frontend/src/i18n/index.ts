import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import { de } from './locales/de'
import { en } from './locales/en'

export const SUPPORTED_LANGUAGES = ['de', 'en'] as const
export type Language = (typeof SUPPORTED_LANGUAGES)[number]

const STORAGE_KEY = 'academy-hub-language'
const DEFAULT_LANGUAGE: Language = 'de'

function isLanguage(value: string | null): value is Language {
  return SUPPORTED_LANGUAGES.includes(value as Language)
}

function readStoredLanguage(): Language {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    return isLanguage(stored) ? stored : DEFAULT_LANGUAGE
  } catch {
    return DEFAULT_LANGUAGE
  }
}

i18n.on('languageChanged', (language) => {
  document.documentElement.lang = language
  try {
    localStorage.setItem(STORAGE_KEY, language)
  } catch {
    // Storage can be unavailable (e.g. private mode); the choice then lasts for this visit only.
  }
})

void i18n.use(initReactI18next).init({
  resources: {
    de: { translation: de },
    en: { translation: en },
  },
  lng: readStoredLanguage(),
  fallbackLng: DEFAULT_LANGUAGE,
  interpolation: {
    escapeValue: false,
  },
})

export default i18n
