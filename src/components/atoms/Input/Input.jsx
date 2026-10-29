export const Input = ({
  as: Element = 'input',
  className = '',
  isInvalid = false,
  ...props
}) => {
  const controlClass = Element === 'select' ? 'form-select' : 'form-control';

  return (
    <Element
      className={`${controlClass} ${isInvalid ? 'is-invalid' : ''} ${className}`.trim()}
      {...props}
    />
  );
};
