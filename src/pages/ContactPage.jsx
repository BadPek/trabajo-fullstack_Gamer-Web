import { useState } from 'react';
import { getEmailSuggestions, isAllowedEmail } from '@/utils/emailUtils';
import { Input } from '@/components/atoms/Input/Input';
import { FormField } from '@/components/molecules/FormField/FormField';

const initialForm = { nombre: '', correo: '', asunto: '', mensaje: '' };
const successMessage = '¡Mensaje enviado con éxito! Nos pondremos en contacto contigo pronto.';

export const ContactPage = () => {
  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: '' }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const nextErrors = {};
    const nombre = form.nombre.trim();
    const correo = form.correo.trim();
    const asunto = form.asunto.trim();
    const mensaje = form.mensaje.trim();

    if (!nombre) nextErrors.nombre = 'Debes ingresar tu nombre.';
    else if (nombre.length > 100) nextErrors.nombre = 'El nombre no puede tener más de 100 caracteres.';

    if (!correo) nextErrors.correo = 'Debes ingresar tu correo.';
    else if (correo.length > 100) nextErrors.correo = 'El correo no puede superar los 100 caracteres.';
    else if (!isAllowedEmail(correo)) nextErrors.correo = 'Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com.';

    if (!asunto) nextErrors.asunto = 'Debes ingresar un asunto.';

    if (!mensaje) nextErrors.mensaje = 'Debes escribir un mensaje.';
    else if (mensaje.length > 500) nextErrors.mensaje = 'El mensaje no puede superar los 500 caracteres.';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;
    window.alert(successMessage);
    setForm(initialForm);
  };

  return (
    <div className="row justify-content-center">
      <div className="col-12 col-md-8 col-lg-6">
        <div className="card border-0 shadow-sm p-4 p-md-5 rounded-4">
          <div className="text-center mb-4">
            <div className="fs-1 text-purple mb-2"><i className="bi bi-envelope-paper-heart"></i></div>
            <h1 className="h2 text-purple fw-bold mb-1">Contáctanos</h1>
            <p className="text-secondary small">Envíanos un mensaje y te responderemos a la brevedad.</p>
          </div>

          <form onSubmit={handleSubmit} noValidate>
            <FormField
              id="contact-name"
              label="Nombre completo"
              required
              error={errors.nombre}
              control={<Input maxLength={100} placeholder="Ej: Sara Morales" value={form.nombre} onChange={(event) => updateField('nombre', event.target.value)} required />}
            />
            <FormField
              id="contact-email"
              label="Correo electrónico"
              required
              error={errors.correo}
              hint="Dominios permitidos: @duoc.cl, @profesor.duoc.cl y @gmail.com"
              control={(
                <Input
                  type="email"
                  list="contact-email-suggestions"
                  autoComplete="email"
                  maxLength={100}
                  placeholder="usuario@duoc.cl o @gmail.com"
                  value={form.correo}
                  onChange={(event) => updateField('correo', event.target.value)}
                  required
                />
              )}
            >
              <datalist id="contact-email-suggestions">
                {getEmailSuggestions(form.correo).map((suggestion) => <option value={suggestion} key={suggestion} />)}
              </datalist>
            </FormField>
            <FormField
              id="contact-subject"
              label="Asunto"
              required
              error={errors.asunto}
              control={<Input value={form.asunto} onChange={(event) => updateField('asunto', event.target.value)} required />}
            />
            <FormField
              id="contact-message"
              label="Mensaje"
              required
              className="mb-4"
              error={errors.mensaje}
              control={<Input as="textarea" rows="4" maxLength={500} placeholder="Escribe tu mensaje aquí..." value={form.mensaje} onChange={(event) => updateField('mensaje', event.target.value)} required />}
            >
              <div className="form-text text-end">{form.mensaje.length}/500</div>
            </FormField>
            <button type="submit" className="btn btn-purple btn-lg w-100 fw-bold">
              <i className="bi bi-send me-1"></i>Enviar mensaje
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
