// Shapes of the dataset-manager and generator APIs (the Java DTOs), discriminated as Jackson serialises them.

export interface Project {
  id: number;
  name: string;
  dateCreated: string;
  lastUpdated: string;
}

export type DataType =
  | { typeClass: 'StringType' }
  | { typeClass: 'IntegerType' }
  | { typeClass: 'FloatType' }
  | { typeClass: 'BooleanType' }
  | { typeClass: 'DateType' }
  | { typeClass: 'EnumType'; values: string[] };

export type TypeClass = DataType['typeClass'];

export type Constraint =
  | { constraintClass: 'MinMaxConstraint'; min: number; max: number }
  | { constraintClass: 'EnumConstraint'; allowed: string[] };

export interface Attribute {
  id?: number;
  name: string;
  type: DataType;
  constraints: Constraint[];
}

export interface DataEntity {
  id: number;
  name: string;
  quantity: number;
  projectId: number;
  attributes: Attribute[];
  subEntities: DataEntity[];
}

/** Body of POST /projects/{id}/entities, POST /entities/{id}/sub-entities and PUT /entities/{id}. */
export interface DataEntityRequest {
  name: string;
  quantity: number;
  attributes: Attribute[];
  subEntities: DataEntityRequest[];
}

export interface PreviewRecord {
  fields: Record<string, unknown>;
}

export interface Preview {
  projectId: number;
  projectName: string;
  status: 'OK' | 'PARTIAL';
  message?: string;
  data: { entityName: string; records: PreviewRecord[] }[];
}

export type ExportFormat = 'sql' | 'json' | 'csv' | 'xml';
