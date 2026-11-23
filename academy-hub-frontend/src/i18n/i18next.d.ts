import 'i18next'
import type { Translation } from './locales/de'

// Makes t('...') keys type-checked against the German translation file.
declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: 'translation'
    resources: {
      translation: Translation
    }
  }
}
