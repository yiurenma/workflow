import React, { useEffect } from "react";
import { Form, Input } from "antd";
import {
  FunctionFormValues,
  FunctionPluginFormProps,
} from "@/types/plugin-form";

const FunctionForm: React.FC<FunctionPluginFormProps> = ({
  selectedNode,
  onValuesChange,
}) => {
  const [form] = Form.useForm();

  // Initialize form with node data
  useEffect(() => {
    if (selectedNode && selectedNode.data) {
      form.setFieldsValue({
        provider: selectedNode.data.provider,
        type: selectedNode.data.label,
        remark: selectedNode.data.remark,
        logic: selectedNode.data.logic,
      });
    }
  }, [selectedNode, form]);

  // Handle form values change
  const handleValuesChange = (
    _changedValues: unknown,
    allValues: FunctionFormValues
  ) => {
    if (onValuesChange) {
      onValuesChange(allValues);
    }
  };

  return (
    <Form form={form} layout="vertical" onValuesChange={handleValuesChange}>
      <Form.Item
        name="provider"
        label="Provider"
        rules={[{ required: true, message: "Please enter a provider" }]}
      >
        <Input placeholder="Enter provider" />
      </Form.Item>
      <Form.Item
        name="type"
        label="Type"
        initialValue={selectedNode?.data.label as string}
      >
        <Input disabled />
      </Form.Item>

      <Form.Item name="remark" label="Remark">
        <Input.TextArea rows={4} placeholder="Enter remark" />
      </Form.Item>
      <Form.Item name="logic" label="Logic">
        <Input.TextArea rows={9} placeholder="Enter logic" />
      </Form.Item>
    </Form>
  );
};

export default FunctionForm;
