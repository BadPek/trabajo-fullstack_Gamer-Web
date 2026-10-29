import { cloneElement, isValidElement } from 'react';

export const FormField = ({
  id,
  label,
  required = false,
  hint,
  error,
  control,
  append,
  children,
  className = 'mb-3',
  labelClassName = 'form-label fw-semibold',
}) => {
  if (!isValidElement(control)) {
    throw new Error('FormField requiere un control React válido.');
  }

  const hintId = hint ? `${id}-hint` : undefined;
  const errorId = error ? `${id}-error` : undefined;
  const describedBy = [control.props['aria-describedby'], hintId, errorId].filter(Boolean).join(' ');
  const fieldControl = cloneElement(control, {
    id,
    isInvalid: Boolean(error) || control.props.isInvalid,
    'aria-invalid': error ? 'true' : control.props['aria-invalid'],
    'aria-describedby': describedBy || undefined,
  });

  return (
    <div className={className}>
      <label htmlFor={id} className={labelClassName}>
        {label} {required && <span className="text-danger">*</span>}
      </label>
      {append ? (
        <div className={`input-group ${error ? 'has-validation' : ''}`}>
          {fieldControl}
          {append}
        </div>
      ) : fieldControl}
      {children}
      {hint && <div id={hintId} className="form-text small">{hint}</div>}
      {error && <div id={errorId} className={`invalid-feedback ${append ? 'd-block' : ''}`}>{error}</div>}
    </div>
  );
};
