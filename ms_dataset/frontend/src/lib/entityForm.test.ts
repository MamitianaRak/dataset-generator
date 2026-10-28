import { describe, expect, it } from 'vitest';
import type { DataEntity } from '../api/types';
import { demoEntity, emptyAttribute, flattenEntities, previewCountLabel, toEntityForm, toEntityRequest } from './entityForm';

const entity = (name: string, subEntities: DataEntity[] = []): DataEntity => ({
  id: name.length, name, quantity: 10, projectId: 1, attributes: [], subEntities,
});

describe('toEntityRequest', () => {
  it('builds the payload the API expects, with a range constraint on numbers', () => {
    expect(toEntityRequest(demoEntity())).toEqual({
      name: 'Utilisateur',
      quantity: 5,
      subEntities: [],
      attributes: [
        { name: 'fullName', type: { typeClass: 'StringType' }, constraints: [] },
        { name: 'email', type: { typeClass: 'StringType' }, constraints: [] },
        { name: 'age', type: { typeClass: 'IntegerType' },
          constraints: [{ constraintClass: 'MinMaxConstraint', min: 18, max: 65 }] },
      ],
    });
  });

  it('splits enum values and drops the unnamed rows of the form', () => {
    const request = toEntityRequest({
      name: ' order ', quantity: '3',
      attributes: [{ ...emptyAttribute(), name: 'status', typeClass: 'EnumType', enumValues: 'PAID, , SHIPPED ' }, emptyAttribute()],
    });

    expect(request.name).toBe('order');
    expect(request.attributes).toEqual([
      { name: 'status', type: { typeClass: 'EnumType', values: ['PAID', 'SHIPPED'] }, constraints: [] },
    ]);
  });
});

describe('toEntityForm', () => {
  it('round-trips with toEntityRequest', () => {
    const request = toEntityRequest(demoEntity());
    const saved: DataEntity = { ...request, id: 1, projectId: 1, subEntities: [] };

    expect(toEntityRequest(toEntityForm(saved))).toEqual(request);
  });
});

describe('flattenEntities', () => {
  it('lists parents before their sub-entities, with depth and parent name', () => {
    const rows = flattenEntities([entity('customer', [entity('order', [entity('line')])]), entity('product')]);

    expect(rows.map(r => [r.name, r.depth, r.parentName])).toEqual([
      ['customer', 0, null],
      ['order', 1, 'customer'],
      ['line', 2, 'order'],
      ['product', 0, null],
    ]);
  });
});

describe('previewCountLabel', () => {
  it('says when the export holds more rows than the preview', () => {
    expect(previewCountLabel(100, 1000)).toBe("aperçu : 100 lignes sur 1000 à l'export");
    expect(previewCountLabel(25, 25)).toBe('25 lignes');
  });
});
