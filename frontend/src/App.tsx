import { useEffect, useMemo, useState } from "react";

type TenantConfig = {
  tenantId: string;
  vatRateBps: number;
  vatRatePercent: string;
};

const API_BASE = "http://localhost:8080";

function App() {
  const [token, setToken] = useState("");
  const [config, setConfig] = useState<TenantConfig | null>(null);
  const [vatPercent, setVatPercent] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem("accessToken");
    if (stored) {
      setToken(stored);
    }
  }, []);

  const authHeaders = useMemo(() => {
    if (!token) {
      return {};
    }
    return { Authorization: `Bearer ${token}` };
  }, [token]);

  const saveToken = () => {
    if (!token.trim()) {
      setStatus("Ingresa un token válido.");
      return;
    }
    localStorage.setItem("accessToken", token.trim());
    setStatus("Token guardado.");
  };

  const loadConfig = async () => {
    setStatus("");
    if (!token) {
      setStatus("Debes pegar un token antes de consultar.");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/tenant-config`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Error al consultar configuración.");
      }
      const data: TenantConfig = await res.json();
      setConfig(data);
      setVatPercent(data.vatRatePercent);
      setStatus("IVA cargado.");
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setLoading(false);
    }
  };

  const updateVat = async () => {
    setStatus("");
    if (!token) {
      setStatus("Debes pegar un token antes de actualizar.");
      return;
    }
    const parsed = Number(vatPercent);
    if (Number.isNaN(parsed)) {
      setStatus("IVA inválido. Usa formato 15.00");
      return;
    }
    const bps = Math.round(parsed * 100);
    if (bps < 0 || bps > 2500) {
      setStatus("IVA fuera de rango (0.00 a 25.00).");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/tenant-config/vat`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify({ vatRateBps: bps }),
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Error al actualizar IVA.");
      }
      const data: TenantConfig = await res.json();
      setConfig(data);
      setVatPercent(data.vatRatePercent);
      setStatus("IVA actualizado.");
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-4xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs uppercase tracking-widest text-slate-500">
              sisferrete
            </p>
            <h1 className="text-lg font-semibold">Configuración crítica</h1>
          </div>
        </div>
      </header>

      <main className="mx-auto grid max-w-4xl gap-6 px-6 py-8">
        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">Token de acceso</h2>
          <p className="mt-1 text-sm text-slate-500">
            Pega un access token con permiso CONFIG_VAT_EDIT.
          </p>
          <div className="mt-4 grid gap-3">
            <input
              value={token}
              onChange={(event) => setToken(event.target.value)}
              placeholder="Bearer token"
              className="h-10 rounded-md border border-slate-200 px-3 text-sm"
            />
            <button
              type="button"
              onClick={saveToken}
              className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
            >
              Guardar token
            </button>
          </div>
        </section>

        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">IVA (tenant)</h2>
          <p className="mt-1 text-sm text-slate-500">
            IVA actual y actualización en basis points (bps).
          </p>
          <div className="mt-4 grid gap-4">
            <div className="text-sm text-slate-700">
              IVA actual:{" "}
              <span className="font-semibold">
                {config ? `${config.vatRatePercent}%` : "--"}
              </span>{" "}
              <span className="text-slate-400">
                {config ? `(${config.vatRateBps} bps)` : ""}
              </span>
            </div>
            <div className="grid gap-2">
              <label className="text-sm font-medium text-slate-700">
                Nuevo IVA (%)
              </label>
              <input
                value={vatPercent}
                onChange={(event) => setVatPercent(event.target.value)}
                placeholder="15.00"
                className="h-10 rounded-md border border-slate-200 px-3 text-sm"
              />
            </div>
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={loadConfig}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={loading}
              >
                Ver IVA
              </button>
              <button
                type="button"
                onClick={updateVat}
                className="h-10 rounded-md bg-emerald-600 px-4 text-sm font-semibold text-white"
                disabled={loading}
              >
                Actualizar IVA
              </button>
            </div>
            {status && <p className="text-sm text-slate-600">{status}</p>}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
