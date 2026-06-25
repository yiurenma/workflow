#!/usr/bin/env python3
"""AI 网关 —— 给本地 Ollama 套一层「鉴权 + 公网接口」。

workflow UI 的聊天框带着登录用户的 Clerk JWT（或先用共享 API Key）调本网关，
验证通过后转发到本机 Ollama 的 OpenAI 兼容接口；Ollama 本身只监听 localhost，绝不直接暴露。

鉴权模式（AUTH_MODE）：
  - clerk   ：校验 Clerk 签发的 JWT（推荐，最终形态）
  - apikey  ：校验共享密钥 GATEWAY_API_KEY（最简，先跑通用）
  - both    ：两者其一通过即可（过渡期）
  - none    ：不鉴权（仅本地调试，切勿暴露公网）

跑起来：
    pip install -r requirements.txt
    cp .env.example .env   # 填好后
    uvicorn app:app --host 127.0.0.1 --port 8800
然后用 Cloudflare Tunnel 把 127.0.0.1:8800 暴露成 https://ai.你的域名（见 README.md）。
"""
import os

import httpx
from fastapi import FastAPI, Header, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, StreamingResponse

try:
    from dotenv import load_dotenv
    load_dotenv()
except Exception:  # noqa: BLE001
    pass

OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://127.0.0.1:11434").rstrip("/")
AUTH_MODE = os.getenv("AUTH_MODE", "apikey").lower()
GATEWAY_API_KEY = os.getenv("GATEWAY_API_KEY", "")
CLERK_ISSUER = os.getenv("CLERK_ISSUER", "").rstrip("/")
CLERK_JWKS_URL = os.getenv("CLERK_JWKS_URL", "") or (
    f"{CLERK_ISSUER}/.well-known/jwks.json" if CLERK_ISSUER else ""
)
CLERK_AUTHORIZED_PARTIES = [
    p.strip() for p in os.getenv("CLERK_AUTHORIZED_PARTIES", "").split(",") if p.strip()
]
ALLOWED_ORIGINS = [
    o.strip() for o in os.getenv("ALLOWED_ORIGINS", "*").split(",") if o.strip()
] or ["*"]

app = FastAPI(title="Workflow AI Gateway")
app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type"],
)

# ---- Clerk JWT 校验（无网络往返：用缓存的 JWKS 公钥本地验签）----
_jwk_client = None


def _jwk():
    global _jwk_client
    if _jwk_client is None:
        from jwt import PyJWKClient  # 延迟导入，apikey 模式无需装 crypto
        _jwk_client = PyJWKClient(CLERK_JWKS_URL)
    return _jwk_client


def _verify_clerk(token: str) -> dict:
    import jwt
    signing_key = _jwk().get_signing_key_from_jwt(token).key
    claims = jwt.decode(
        token,
        signing_key,
        algorithms=["RS256"],
        issuer=CLERK_ISSUER or None,
        options={"require": ["exp"], "verify_aud": False},
    )
    # 授权方(azp)校验：只接受来自 workflow UI 源的令牌
    if CLERK_AUTHORIZED_PARTIES:
        azp = claims.get("azp")
        if azp and azp not in CLERK_AUTHORIZED_PARTIES:
            raise ValueError(f"azp {azp!r} 不在授权列表内")
    return claims


def _authorize(authorization: str | None) -> dict:
    if AUTH_MODE == "none":
        return {"sub": "anonymous"}
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(401, "缺少 Authorization: Bearer 令牌")
    token = authorization.split(" ", 1)[1].strip()

    if AUTH_MODE in ("apikey", "both") and GATEWAY_API_KEY and token == GATEWAY_API_KEY:
        return {"sub": "apikey"}
    if AUTH_MODE in ("clerk", "both"):
        if not CLERK_JWKS_URL:
            raise HTTPException(500, "未配置 CLERK_ISSUER / CLERK_JWKS_URL")
        try:
            return _verify_clerk(token)
        except Exception as e:  # noqa: BLE001
            raise HTTPException(401, f"令牌无效: {e}")
    raise HTTPException(401, "未授权")


@app.get("/healthz")
def healthz():
    return {"ok": True, "auth_mode": AUTH_MODE}


@app.get("/v1/models")
async def models(authorization: str | None = Header(default=None)):
    _authorize(authorization)
    async with httpx.AsyncClient(timeout=30) as client:
        r = await client.get(f"{OLLAMA_BASE_URL}/v1/models")
    return JSONResponse(status_code=r.status_code, content=r.json())


@app.post("/v1/chat/completions")
async def chat_completions(request: Request, authorization: str | None = Header(default=None)):
    _authorize(authorization)
    body = await request.body()
    url = f"{OLLAMA_BASE_URL}/v1/chat/completions"

    async def upstream():
        async with httpx.AsyncClient(timeout=None) as client:
            async with client.stream(
                "POST", url, content=body, headers={"Content-Type": "application/json"}
            ) as r:
                async for chunk in r.aiter_raw():
                    yield chunk

    # 流式 / 非流式都用原样透传；SSE 与 JSON 客户端都能正确解析
    is_stream = b'"stream"' in body and b'"stream": false' not in body and b'"stream":false' not in body
    media = "text/event-stream" if is_stream else "application/json"
    return StreamingResponse(upstream(), media_type=media)
