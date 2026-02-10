import { Button } from "@/components/ui/button";

const menuItems = ["POS", "Productos", "Inventario", "Compras", "Reportes"];

function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs uppercase tracking-widest text-slate-500">
              sisferrete
            </p>
            <h1 className="text-lg font-semibold">Panel base</h1>
          </div>
          <Button variant="outline">Ayuda</Button>
        </div>
      </header>

      <main className="mx-auto grid max-w-6xl gap-6 px-6 py-8 md:grid-cols-[1fr_1fr]">
        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">Iniciar sesión</h2>
          <p className="mt-1 text-sm text-slate-500">
            Pantalla base sin autenticación real (placeholder).
          </p>

          <div className="mt-6 grid gap-4">
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Correo
              <input
                type="email"
                placeholder="usuario@ferreteria.com"
                className="h-10 rounded-md border border-slate-200 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-slate-300"
              />
            </label>
            <label className="grid gap-2 text-sm font-medium text-slate-700">
              Contraseña
              <input
                type="password"
                placeholder="••••••••"
                className="h-10 rounded-md border border-slate-200 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-slate-300"
              />
            </label>
            <Button>Ingresar</Button>
          </div>
        </section>

        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">Shell base</h2>
          <p className="mt-1 text-sm text-slate-500">
            Menú lateral placeholder para el POS y backoffice.
          </p>
          <nav className="mt-6 grid gap-2">
            {menuItems.map((item) => (
              <div
                key={item}
                className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2 text-sm"
              >
                <span>{item}</span>
                <span className="text-xs text-slate-400">próximo</span>
              </div>
            ))}
          </nav>
        </section>
      </main>
    </div>
  );
}

export default App;