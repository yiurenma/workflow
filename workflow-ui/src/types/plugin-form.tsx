import { Node } from "@xyflow/react";

export interface PluginBaseFormValues {
  provider: string;
  type: string;
  remark?: string;
}

export interface HTTPBaseFormValues extends PluginBaseFormValues {
  method: "GET" | "POST" | "PUT" | "DELETE";
  url: string;
  internalRequestUrl: string;
  headers: Record<string, string>;
  body?: string;
  params?: Record<string, string>;
  response: string;
}

export interface LogicBaseFormValues extends PluginBaseFormValues {
  logic: string;
}

export type ConsumerFormValues = HTTPBaseFormValues;
export type MessageFormValues = HTTPBaseFormValues;
export type FunctionFormValues = LogicBaseFormValues;
export type IfElseFormValues = LogicBaseFormValues;

export type PluginFormData =
  | ConsumerFormValues
  | MessageFormValues
  | FunctionFormValues
  | IfElseFormValues;

export interface PluginFormBasedProps<Values = PluginFormData> {
  selectedNode: Node;
  onValuesChange?: (values: Values) => void;
}

export type ConsumerPluginFormProps = PluginFormBasedProps<ConsumerFormValues>;
export type MessagePluginFormProps = PluginFormBasedProps<MessageFormValues>;
export type FunctionPluginFormProps = PluginFormBasedProps<FunctionFormValues>;
export type IfElsePluginFormProps = PluginFormBasedProps<IfElseFormValues>;
