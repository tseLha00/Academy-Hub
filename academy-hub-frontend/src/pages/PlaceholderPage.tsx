import { useTranslation } from 'react-i18next'

export function PlaceholderPage() {
  const { t } = useTranslation()

  return <p>{t('placeholder.notCreated')}</p>
}
