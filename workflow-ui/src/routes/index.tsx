import { createFileRoute, Link } from "@tanstack/react-router";
import { Button, Typography } from "antd";

export const Route = createFileRoute("/")({
  component: Index,
});

function Index() {
  return (
    <div className="p-8 max-w-xl">
      <Typography.Title level={3} className="!mt-0">
        Workflow UI
      </Typography.Title>
      <Typography.Paragraph type="secondary">
        Manage applications, edit workflow definitions, and run test requests
        against the online API.
      </Typography.Paragraph>
      <Link to="/workflows">
        <Button type="primary">Go to applications</Button>
      </Link>
    </div>
  );
}
