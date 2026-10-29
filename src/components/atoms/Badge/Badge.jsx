const variantClasses = {
  purple: 'bg-purple text-white',
  'purple-light': 'bg-purple-light text-purple',
  success: 'bg-success-subtle text-success',
  dark: 'bg-dark text-white',
};

export const Badge = ({ children, variant = 'purple-light', className = '', ...props }) => (
  <span className={`badge ${variantClasses[variant] || variantClasses['purple-light']} ${className}`.trim()} {...props}>
    {children}
  </span>
);
