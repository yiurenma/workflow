#!/usr/bin/env python3
"""查询 Workflow 运行记录（只读，安全）。

帮用户排查「我那次调用为什么失败」。调用 operation-api 的
GET /api/workflow/records。

用法:
    python query_records.py --app DEMO_PAY
    python query_records.py --app DEMO_PAY --confirmation PAY-20260621-0001
    python query_records.py --app DEMO_PAY --status FAILED --size 5

环境变量:
    WORKFLOW_OPERATION_API   operation-api base URL
                             (默认 https://workflow-operation-api-n9sbp.ondigitalocean.app)
"""
import argparse
import json
import os
import sys
import urllib.parse
import urllib.request

DEFAULT_OPERATION_API = "https://workflow-operation-api-n9sbp.ondigitalocean.app"


def query_records(base_url, app, confirmation=None, status=None, page=0, size=10):
    params = {"applicationName": app, "page": page, "size": size}
    if confirmation:
        params["confirmationNumber"] = confirmation
    if status:
        params["overallStatus"] = status
    url = f"{base_url.rstrip('/')}/api/workflow/records?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main():
    p = argparse.ArgumentParser(description="查询 Workflow 运行记录（只读）")
    p.add_argument("--app", required=True, help="applicationName，如 DEMO_PAY")
    p.add_argument("--confirmation", help="confirmationNumber 业务关联值")
    p.add_argument("--status", help="overallStatus 过滤，如 SUCCESS / FAILED")
    p.add_argument("--page", type=int, default=0)
    p.add_argument("--size", type=int, default=10)
    args = p.parse_args()

    base = os.environ.get("WORKFLOW_OPERATION_API", DEFAULT_OPERATION_API)
    try:
        data = query_records(base, args.app, args.confirmation, args.status, args.page, args.size)
    except Exception as e:  # noqa: BLE001
        print(f"查询失败: {e}", file=sys.stderr)
        sys.exit(1)
    print(json.dumps(data, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
