import { typeLabel, type EntityRow } from '../lib/entityForm';
import { Icon } from './Icon';

interface EntityTableProps {
  rows: EntityRow[];
  onAddChild: (row: EntityRow) => void;
  onEdit: (row: EntityRow) => void;
  onDelete: (row: EntityRow) => void;
}

/** Tables and sub-tables as a tree; each column shows its type, a sub-table its foreign key. */
export function EntityTable({ rows, onAddChild, onEdit, onDelete }: EntityTableProps) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Table</th>
          <th>Lignes</th>
          <th>Colonnes</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {rows.map(row => (
          <tr key={row.id}>
            <td>
              <div className="entity-cell" style={{ paddingLeft: `${row.depth * 22}px` }}>
                {row.depth > 0 && <span className="entity-tree">└</span>}
                <span className="entity-icon"><Icon name="table" size={15} /></span>
                <div>
                  <div className="entity-name">{row.name}</div>
                  {row.parentName && (
                    <div className="fk-note"><Icon name="link" size={12} /> {row.parentName}_id → {row.parentName}.id</div>
                  )}
                </div>
              </div>
            </td>
            <td className="qty">{row.quantity.toLocaleString('fr-FR')}</td>
            <td>
              <div className="attr-tags">
                {row.attributes.map(attribute => (
                  <span className="attr-tag" key={attribute.name}>
                    <span className={`type-dot type-${attribute.type.typeClass}`}>{typeLabel(attribute.type.typeClass)}</span>
                    {attribute.name}
                  </span>
                ))}
              </div>
            </td>
            <td>
              <div className="row-actions">
                <button className="btn btn-ghost" title="Ajouter une table liée à celle-ci" onClick={() => onAddChild(row)}>
                  <Icon name="plus" size={14} /> Sous-table
                </button>
                <button className="btn btn-ghost" title="Modifier" onClick={() => onEdit(row)}><Icon name="edit" size={14} /></button>
                <button className="btn btn-danger" title="Supprimer, avec ses sous-tables" onClick={() => onDelete(row)}>
                  <Icon name="trash" size={14} />
                </button>
              </div>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
