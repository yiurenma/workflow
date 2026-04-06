import { Node, Edge } from '@xyflow/react';
import React from 'react';

export type WorkflowType = 'Message';

export type WorkflowStatus = 'draft' | 'published';

export type NodeFormData = {
  label: string;
  icon?: React.ReactNode;
  backendPlugin?: import('./operation').BackendPlugin;
  [key: string]: string | number | boolean | React.ReactNode | import('./operation').BackendPlugin | undefined;
};

export interface WorkflowNode extends Node {
  data: NodeFormData;
}

export type WorkflowEdge = Edge;

/** Create / rename dialog payload */
export interface ApplicationFormValues {
  applicationName: string;
}

/** Editor state: full workflow from server + canvas sync */
export interface WorkflowEditorModel {
  applicationName: string;
  workFlow: import('./operation').WorkFlow;
}
