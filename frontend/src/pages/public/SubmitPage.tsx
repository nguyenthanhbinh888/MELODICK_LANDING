import React from 'react';
import { useSiteSettings } from '../../app/providers/SiteSettingsContext';
import './SubmitPage.css';

export const SubmitPage: React.FC = () => {
  const { settings } = useSiteSettings();

  const description = settings.submit_description || 'Looking for new signatures.';
  const buttonLabel = settings.submit_button_label || 'SUBMIT DEMO';
  const submitUrl = settings.submit_url?.trim() || '';

  const isValidUrl =
    submitUrl.length > 0 &&
    submitUrl !== '#' &&
    (submitUrl.startsWith('http://') || submitUrl.startsWith('https://'));

  return (
    <section className="public-page-section" id="submit">
      <div className="submit-container">
        <p className="submit-text">{description}</p>
        {isValidUrl ? (
          <a
            href={submitUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="btn-submit"
          >
            {buttonLabel}
          </a>
        ) : (
          <p className="submit-closed-notice">Cổng nhận demo hiện đang tạm đóng.</p>
        )}
      </div>
    </section>
  );
};
