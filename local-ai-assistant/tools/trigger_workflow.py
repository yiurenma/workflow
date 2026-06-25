#!/usr/bin/env python3
"""触发已发布工作流（⚠️ 写操作，会真的跑线上流程）。

调用 online-api 的 POST /api/workflow，自动带上必需的
X-Request-Correlation-Id（幂等键）。默认有两道保险：
  1) 白名单：只允许 WORKFLOW_ALLOWED_APPS 里列出的 applicationName；
  2) 二次确认：必须交互式输入 yes，或显式 --yes。

用法:
    python trigger_workflow.py --app DEMO_PAY --confirmation PAY-20260625-0001 \
        --body '{"customerId":"C-1001","amount":8800,"currency":"AUD"}'

环境变量:
    WORKFLOW_ONLINE_API    online-api base URL
                           (默认 https://workflow-online-api-nr3e4.ondigitalocean.app)
    WORKFLOW_ALLOWED_APPS  允许触发的应用名，逗号分隔；为空表示不限制（不建议）
"""
import argparse
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid

DEFAULT_ONLINE_API = "https://workflow-online-api-nr3e4.ondigitalocean.app"


def trigger(base_url, app, confirmation, body, correlation_id, channel_kind=None):
    params = {"applicationName": app, "confirmationNumber": confirmation}
    if channel_kind:
        params["channelKind"] = channel_kind
    url = f"{base_url.rstrip('/')}/api/workflow?" + urllib.parse.urlencode(params)
    headers = {
        "Content-Type": "application/json",
        "X-Request-Correlation-Id": correlation_id,  # 必需：幂等/重复检测
    }
    data = body.encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=headers, method="POST")
    with urllib.request.urlopen(req, timeout=30) as resp:
        return resp.status, resp.read().decode("utf-8")


def main():
    p = argparse.ArgumentParser(description="触发已发布工作流（写操作）")
    p.add_argument("--app", required=True, help="applicationName")
    p.add_argument("--confirmation", required=True, help="confirmationNumber 业务关联值")
    p.add_argument("--body", required=True, help="业务 JSON 字符串，作为 ingressBody")
    p.add_argument("--channel-kind", help="可选通道提示，如 SMS / Email")
    p.add_argument("--correlation-id", help="幂等键；默认自动生成 UUID")
    p.add_argument("--yes", action="store_true", help="跳过二次确认（自动化时用，慎用）")
    args = p.parse_args()

    # 校验 body 是合法 JSON
    try:
        json.loads(args.body)
    except json.JSONDecodeError as e:
        print(f"--body 不是合法 JSON: {e}", file=sys.stderr)
        sys.exit(2)

    # 白名单
    allowed = [s.strip() for s in os.environ.get("WORKFLOW_ALLOWED_APPS", "").split(",") if s.strip()]
    if allowed and args.app not in allowed:
        print(f"拒绝：{args.app} 不在白名单 WORKFLOW_ALLOWED_APPS={allowed}", file=sys.stderr)
        sys.exit(3)

    correlation_id = args.correlation_id or str(uuid.uuid4())
    base = os.environ.get("WORKFLOW_ONLINE_API", DEFAULT_ONLINE_API)

    # 二次确认
    if not args.yes:
        print("⚠️  即将触发线上工作流：")
        print(f"    app            = {args.app}")
        print(f"    confirmation   = {args.confirmation}")
        print(f"    correlation-id = {correlation_id}")
        print(f"    body           = {args.body}")
        print(f"    endpoint       = {base}")
        if input("确认执行？输入 yes 继续：").strip().lower() != "yes":
            print("已取消。")
            sys.exit(0)

    try:
        status, text = trigger(base, args.app, args.confirmation, args.body,
                               correlation_id, args.channel_kind)
    except urllib.error.HTTPError as e:
        print(f"HTTP {e.code}: {e.read().decode('utf-8', 'replace')}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:  # noqa: BLE001
        print(f"触发失败: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"HTTP {status}  (200=成功，空体；结果请用 query_records.py 查运行记录)")
    if text.strip():
        print(text)


if __name__ == "__main__":
    main()
