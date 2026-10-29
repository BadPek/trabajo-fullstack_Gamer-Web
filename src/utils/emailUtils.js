const allowedDomains = ['duoc.cl', 'profesor.duoc.cl', 'gmail.com'];
const emailPattern = /^[a-zA-Z0-9._%+-]+@(duoc\.cl|profesor\.duoc\.cl|gmail\.com)$/i;

export const isAllowedEmail = (email) => emailPattern.test(email);

export const getEmailSuggestions = (value) => {
  const [username, typedDomain = ''] = value.trim().split('@');
  if (!username) return [];

  return allowedDomains
    .filter((domain) => domain.startsWith(typedDomain.toLowerCase()))
    .map((domain) => `${username}@${domain}`);
};
