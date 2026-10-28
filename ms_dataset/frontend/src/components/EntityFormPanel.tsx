import { useState, type FormEvent } from 'react';
import { emptyAttribute, TYPE_OPTIONS, type AttributeForm, type EntityForm } from '../lib/entityForm';
import { Icon } from './Icon';

/** What the form does on submit: create a table, add a sub-table to a parent, or update a table. */
export type FormMode =
  | { kind: 'create' }
  | { kind: 'createChild'; parentId: number; parentName: string }
  | { kind: 'edit'; entityId: number };

interface EntityFormPanelProps {
  mode: FormMode;
  initial: EntityForm;
  onSubmit: (form: EntityForm) => Promise<void>;
  onCancel: () => void;
}

function title(mode: FormMode): string {
  switch (mode.kind) {
    case 'create': return 'Nouvelle table';
    case 'createChild': return `Nouvelle sous-table de ${mode.parentName}`;
    case 'edit': return 'Modifier la table';
  }
}

export function EntityFormPanel({ mode, initial, onSubmit, onCancel }: EntityFormPanelProps) {
  const [form, setForm] = useState<EntityForm>(initial);

  const updateAttribute = (index: number, patch: Partial<AttributeForm>) =>
    setForm(f => ({ ...f, attributes: f.attributes.map((a, i) => (i === index ? { ...a, ...patch } : a)) }));

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (form.name.trim()) await onSubmit(form);
  }

  return (
    <form onSubmit={submit} className="entity-form">
      <div className="form-title">{title(mode)}</div>
      {mode.kind === 'createChild' && (
        <p className="form-hint">
          <Icon name="link" size={14} />
          Chaque ligne aura une colonne <code>{mode.parentName}_id</code> qui référence une ligne existante de <code>{mode.parentName}</code>.
        </p>
      )}

      <div className="form-row">
        <div className="field" style={{ flex: 1, minWidth: 220 }}>
          <label htmlFor="entity-name">Nom de la table</label>
          <input id="entity-name" className="input" type="text" required placeholder="ex : client, commande"
                 value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
        </div>
        <div className="field">
          <label htmlFor="entity-quantity">Lignes à générer</label>
          <input id="entity-quantity" className="input input-small" type="number" min="1" max="1000000"
                 value={form.quantity} onChange={e => setForm(f => ({ ...f, quantity: e.target.value }))} />
        </div>
      </div>

      <div className="field-label">Colonnes</div>
      <div className="attribute-grid">
        {form.attributes.map((attribute, index) => (
          <div className="attribute-row" key={index}>
            <input className="input" type="text" placeholder="nom de la colonne" aria-label="Nom de la colonne"
                   value={attribute.name} onChange={e => updateAttribute(index, { name: e.target.value })} />
            <select className="input" value={attribute.typeClass} aria-label="Type"
                    onChange={e => updateAttribute(index, { typeClass: e.target.value as AttributeForm['typeClass'] })}>
              {TYPE_OPTIONS.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
            </select>
            <div className="attribute-extra">
              {(attribute.typeClass === 'IntegerType' || attribute.typeClass === 'FloatType') && (
                <>
                  <span>entre</span>
                  <input className="input" type="number" placeholder="min" aria-label="Minimum" value={attribute.min}
                         onChange={e => updateAttribute(index, { min: e.target.value })} />
                  <span>et</span>
                  <input className="input" type="number" placeholder="max" aria-label="Maximum" value={attribute.max}
                         onChange={e => updateAttribute(index, { max: e.target.value })} />
                </>
              )}
              {attribute.typeClass === 'EnumType' && (
                <input className="input enum-values" type="text" placeholder="valeurs séparées par des virgules"
                       aria-label="Valeurs" value={attribute.enumValues}
                       onChange={e => updateAttribute(index, { enumValues: e.target.value })} />
              )}
            </div>
            <button type="button" className="btn btn-danger" aria-label="Retirer la colonne"
                    disabled={form.attributes.length === 1}
                    onClick={() => setForm(f => ({ ...f, attributes: f.attributes.filter((_, i) => i !== index) }))}>
              <Icon name="x" size={14} />
            </button>
          </div>
        ))}
      </div>

      <div className="btn-group">
        <button type="button" className="btn btn-secondary"
                onClick={() => setForm(f => ({ ...f, attributes: [...f.attributes, emptyAttribute()] }))}>
          <Icon name="plus" size={14} /> Colonne
        </button>
        <span style={{ flex: 1 }} />
        <button type="button" className="btn btn-ghost" onClick={onCancel}>Annuler</button>
        <button type="submit" className="btn btn-primary">{mode.kind === 'edit' ? 'Mettre à jour' : 'Créer la table'}</button>
      </div>
    </form>
  );
}
