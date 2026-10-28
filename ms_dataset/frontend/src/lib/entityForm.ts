import type { Attribute, Constraint, DataEntity, DataEntityRequest, DataType, TypeClass } from '../api/types';

/** An attribute as edited in the form: every field is a string, as the inputs hold them. */
export interface AttributeForm {
  name: string;
  typeClass: TypeClass;
  min: string;
  max: string;
  enumValues: string;
}

export interface EntityForm {
  name: string;
  quantity: string;
  attributes: AttributeForm[];
}

export const TYPE_OPTIONS: { value: TypeClass; label: string }[] = [
  { value: 'StringType', label: 'Texte' },
  { value: 'IntegerType', label: 'Entier' },
  { value: 'FloatType', label: 'Décimal' },
  { value: 'BooleanType', label: 'Booléen' },
  { value: 'DateType', label: 'Date' },
  { value: 'EnumType', label: 'Énumération' },
];

export const typeLabel = (typeClass: TypeClass): string =>
  TYPE_OPTIONS.find(t => t.value === typeClass)?.label ?? typeClass;

export const emptyAttribute =(): AttributeForm => ({ name: '', typeClass: 'StringType', min: '', max: '', enumValues: '' });

export const emptyEntity = (): EntityForm => ({ name: '', quantity: '10', attributes: [emptyAttribute()] });

export const demoEntity = (): EntityForm => ({
  name: 'Utilisateur',
  quantity: '5',
  attributes: [
    { ...emptyAttribute(), name: 'fullName' },
    { ...emptyAttribute(), name: 'email' },
    { ...emptyAttribute(), name: 'age', typeClass: 'IntegerType', min: '18', max: '65' },
  ],
});

const isNumeric = (typeClass: TypeClass) => typeClass === 'IntegerType' || typeClass === 'FloatType';

function toAttribute(form: AttributeForm): Attribute {
  const type: DataType = form.typeClass === 'EnumType'
    ? { typeClass: 'EnumType', values: form.enumValues.split(',').map(v => v.trim()).filter(Boolean) }
    : { typeClass: form.typeClass };
  const constraints: Constraint[] = [];
  if (isNumeric(form.typeClass) && (form.min !== '' || form.max !== '')) {
    const parse = form.typeClass === 'FloatType' ? parseFloat : (v: string) => parseInt(v, 10);
    constraints.push({ constraintClass: 'MinMaxConstraint', min: parse(form.min || '0'), max: parse(form.max || '0') });
  }
  return { name: form.name.trim(), type, constraints };
}

/** Rows without a name are left out: they are the empty line the form always offers. */
export function toEntityRequest(form: EntityForm): DataEntityRequest {
  return {
    name: form.name.trim(),
    quantity: Number(form.quantity) || 1,
    attributes: form.attributes.filter(a => a.name.trim()).map(toAttribute),
    subEntities: [],
  };
}

export function toEntityForm(entity: DataEntity): EntityForm {
  return {
    name: entity.name,
    quantity: String(entity.quantity),
    attributes: entity.attributes.map(attribute => {
      const range = attribute.constraints.find(c => c.constraintClass === 'MinMaxConstraint');
      return {
        name: attribute.name,
        typeClass: attribute.type.typeClass,
        min: range ? String(range.min) : '',
        max: range ? String(range.max) : '',
        enumValues: attribute.type.typeClass === 'EnumType' ? attribute.type.values.join(', ') : '',
      };
    }),
  };
}

/** An entity of the tree, with its depth and its parent's name, for an indented list. */
export interface EntityRow extends DataEntity {
  depth: number;
  parentName: string | null;
}

export function flattenEntities(entities: DataEntity[], depth = 0, parentName: string | null = null): EntityRow[] {
  return entities.flatMap(entity => [
    { ...entity, depth, parentName },
    ...flattenEntities(entity.subEntities ?? [], depth + 1, entity.name),
  ]);
}

/** The preview is capped server-side: say so when the export will hold more rows than shown. */
export function previewCountLabel(previewRows: number, quantity: number | undefined): string {
  return quantity !== undefined && quantity > previewRows
    ? `aperçu : ${previewRows} lignes sur ${quantity} à l'export`
    : `${previewRows} lignes`;
}
