import { useEffect, useMemo, useState } from "react";

type TenantConfig = {
  tenantId: string;
  vatRateBps: number;
  vatRatePercent: string;
};

type IamUser = {
  userId: string;
  email: string;
  fullName: string;
  isActive: boolean;
  roleCodes: string[];
  branchIds: string[];
};

type IamRole = {
  roleId: string;
  code: string;
  name: string;
  description?: string;
  permissionCodes: string[];
};

type IamPermission = {
  code: string;
  name: string;
  description?: string;
  module: string;
};

type IamBranch = {
  branchId: string;
  code: string;
  name: string;
};

const API_BASE = "http://localhost:8080";

function App() {
  const [token, setToken] = useState("");
  const [config, setConfig] = useState<TenantConfig | null>(null);
  const [vatPercent, setVatPercent] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);

  const [iamStatus, setIamStatus] = useState("");
  const [iamLoading, setIamLoading] = useState(false);
  const [users, setUsers] = useState<IamUser[]>([]);
  const [roles, setRoles] = useState<IamRole[]>([]);
  const [permissions, setPermissions] = useState<IamPermission[]>([]);
  const [branches, setBranches] = useState<IamBranch[]>([]);

  const [newUser, setNewUser] = useState({
    email: "",
    fullName: "",
    password: "",
    isActive: true,
  });
  const [updateUser, setUpdateUser] = useState({
    userId: "",
    fullName: "",
    isActive: true,
  });
  const [assignRoles, setAssignRoles] = useState({
    userId: "",
    roleCodes: "",
  });
  const [assignBranches, setAssignBranches] = useState({
    userId: "",
    branchIds: "",
    allBranches: false,
  });

  const [newRole, setNewRole] = useState({
    code: "",
    name: "",
    description: "",
  });
  const [updateRole, setUpdateRole] = useState({
    roleId: "",
    name: "",
    description: "",
  });
  const [assignPermissions, setAssignPermissions] = useState({
    roleId: "",
    permissionCodes: "",
  });

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

  const fetchUsers = async () => {
    setIamStatus("");
    setIamLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/users`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: IamUser[] = await res.json();
      setUsers(data);
      setIamStatus("Usuarios cargados.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const fetchRoles = async () => {
    setIamStatus("");
    setIamLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/roles`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: IamRole[] = await res.json();
      setRoles(data);
      setIamStatus("Roles cargados.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const fetchPermissions = async () => {
    setIamStatus("");
    setIamLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/permissions`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: IamPermission[] = await res.json();
      setPermissions(data);
      setIamStatus("Permisos cargados.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const fetchBranches = async () => {
    setIamStatus("");
    setIamLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/branches`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: IamBranch[] = await res.json();
      setBranches(data);
      setIamStatus("Sucursales cargadas.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const createUser = async () => {
    setIamStatus("");
    setIamLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/users`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify(newUser),
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchUsers();
      setIamStatus("Usuario creado.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const updateUserData = async () => {
    setIamStatus("");
    if (!updateUser.userId) {
      setIamStatus("Debes indicar userId.");
      return;
    }
    setIamLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/users/${updateUser.userId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            fullName: updateUser.fullName,
            isActive: updateUser.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchUsers();
      setIamStatus("Usuario actualizado.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const replaceUserRoles = async () => {
    setIamStatus("");
    if (!assignRoles.userId) {
      setIamStatus("Debes indicar userId.");
      return;
    }
    const codes = assignRoles.roleCodes
      .split(",")
      .map((value) => value.trim())
      .filter(Boolean);
    if (!codes.length) {
      setIamStatus("Ingresa al menos un rol.");
      return;
    }
    setIamLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/users/${assignRoles.userId}/roles`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({ roleCodes: codes }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchUsers();
      setIamStatus("Roles actualizados.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const replaceUserBranches = async () => {
    setIamStatus("");
    if (!assignBranches.userId) {
      setIamStatus("Debes indicar userId.");
      return;
    }
    const branchIds = assignBranches.branchIds
      .split(",")
      .map((value) => value.trim())
      .filter(Boolean);
    setIamLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/users/${assignBranches.userId}/branches`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            branchIds: branchIds,
            allBranches: assignBranches.allBranches,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchUsers();
      setIamStatus("Sucursales asignadas.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const createRole = async () => {
    setIamStatus("");
    setIamLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/roles`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify(newRole),
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchRoles();
      setIamStatus("Rol creado.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const updateRoleData = async () => {
    setIamStatus("");
    if (!updateRole.roleId) {
      setIamStatus("Debes indicar roleId.");
      return;
    }
    setIamLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/roles/${updateRole.roleId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            name: updateRole.name,
            description: updateRole.description,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchRoles();
      setIamStatus("Rol actualizado.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  const replaceRolePermissions = async () => {
    setIamStatus("");
    if (!assignPermissions.roleId) {
      setIamStatus("Debes indicar roleId.");
      return;
    }
    const codes = assignPermissions.permissionCodes
      .split(",")
      .map((value) => value.trim())
      .filter(Boolean);
    if (!codes.length) {
      setIamStatus("Ingresa al menos un permiso.");
      return;
    }
    setIamLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/roles/${assignPermissions.roleId}/permissions`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({ permissionCodes: codes }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchRoles();
      setIamStatus("Permisos actualizados.");
    } catch (err) {
      setIamStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setIamLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs uppercase tracking-widest text-slate-500">
              sisferrete
            </p>
            <h1 className="text-lg font-semibold">
              Admin IAM + Configuración crítica
            </h1>
          </div>
        </div>
      </header>

      <main className="mx-auto grid max-w-6xl gap-6 px-6 py-8">
        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">Token de acceso</h2>
          <p className="mt-1 text-sm text-slate-500">
            Pega un access token con permiso IAM_MANAGE y CONFIG_VAT_EDIT.
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

        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">IAM (usuarios y roles)</h2>
          <p className="mt-1 text-sm text-slate-500">
            Operaciones mínimas para usuarios, roles, permisos y sucursales.
          </p>

          <div className="mt-4 grid gap-4">
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={fetchUsers}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={iamLoading}
              >
                Ver usuarios
              </button>
              <button
                type="button"
                onClick={fetchRoles}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={iamLoading}
              >
                Ver roles
              </button>
              <button
                type="button"
                onClick={fetchPermissions}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={iamLoading}
              >
                Ver permisos
              </button>
              <button
                type="button"
                onClick={fetchBranches}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={iamLoading}
              >
                Ver sucursales
              </button>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear usuario
                </h3>
                <input
                  value={newUser.email}
                  onChange={(event) =>
                    setNewUser((prev) => ({
                      ...prev,
                      email: event.target.value,
                    }))
                  }
                  placeholder="correo@demo.com"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newUser.fullName}
                  onChange={(event) =>
                    setNewUser((prev) => ({
                      ...prev,
                      fullName: event.target.value,
                    }))
                  }
                  placeholder="Nombre completo"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newUser.password}
                  onChange={(event) =>
                    setNewUser((prev) => ({
                      ...prev,
                      password: event.target.value,
                    }))
                  }
                  placeholder="Contraseña"
                  type="password"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newUser.isActive}
                    onChange={(event) =>
                      setNewUser((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={createUser}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                  disabled={iamLoading}
                >
                  Crear usuario
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar usuario
                </h3>
                <input
                  value={updateUser.userId}
                  onChange={(event) =>
                    setUpdateUser((prev) => ({
                      ...prev,
                      userId: event.target.value,
                    }))
                  }
                  placeholder="userId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateUser.fullName}
                  onChange={(event) =>
                    setUpdateUser((prev) => ({
                      ...prev,
                      fullName: event.target.value,
                    }))
                  }
                  placeholder="Nombre completo"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateUser.isActive}
                    onChange={(event) =>
                      setUpdateUser((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={updateUserData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={iamLoading}
                >
                  Guardar cambios
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Asignar roles
                </h3>
                <input
                  value={assignRoles.userId}
                  onChange={(event) =>
                    setAssignRoles((prev) => ({
                      ...prev,
                      userId: event.target.value,
                    }))
                  }
                  placeholder="userId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={assignRoles.roleCodes}
                  onChange={(event) =>
                    setAssignRoles((prev) => ({
                      ...prev,
                      roleCodes: event.target.value,
                    }))
                  }
                  placeholder="ADMIN,CAJERO"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <button
                  type="button"
                  onClick={replaceUserRoles}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={iamLoading}
                >
                  Actualizar roles
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Asignar sucursales
                </h3>
                <input
                  value={assignBranches.userId}
                  onChange={(event) =>
                    setAssignBranches((prev) => ({
                      ...prev,
                      userId: event.target.value,
                    }))
                  }
                  placeholder="userId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={assignBranches.branchIds}
                  onChange={(event) =>
                    setAssignBranches((prev) => ({
                      ...prev,
                      branchIds: event.target.value,
                    }))
                  }
                  placeholder="branchId,branchId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={assignBranches.allBranches}
                    onChange={(event) =>
                      setAssignBranches((prev) => ({
                        ...prev,
                        allBranches: event.target.checked,
                      }))
                    }
                  />
                  Todas las sucursales
                </label>
                <button
                  type="button"
                  onClick={replaceUserBranches}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={iamLoading}
                >
                  Actualizar sucursales
                </button>
              </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear rol
                </h3>
                <input
                  value={newRole.code}
                  onChange={(event) =>
                    setNewRole((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="CAJERO"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newRole.name}
                  onChange={(event) =>
                    setNewRole((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Nombre visible"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newRole.description}
                  onChange={(event) =>
                    setNewRole((prev) => ({
                      ...prev,
                      description: event.target.value,
                    }))
                  }
                  placeholder="Descripción"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <button
                  type="button"
                  onClick={createRole}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                  disabled={iamLoading}
                >
                  Crear rol
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar rol
                </h3>
                <input
                  value={updateRole.roleId}
                  onChange={(event) =>
                    setUpdateRole((prev) => ({
                      ...prev,
                      roleId: event.target.value,
                    }))
                  }
                  placeholder="roleId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateRole.name}
                  onChange={(event) =>
                    setUpdateRole((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Nombre visible"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateRole.description}
                  onChange={(event) =>
                    setUpdateRole((prev) => ({
                      ...prev,
                      description: event.target.value,
                    }))
                  }
                  placeholder="Descripción"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <button
                  type="button"
                  onClick={updateRoleData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={iamLoading}
                >
                  Guardar rol
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Asignar permisos a rol
                </h3>
                <input
                  value={assignPermissions.roleId}
                  onChange={(event) =>
                    setAssignPermissions((prev) => ({
                      ...prev,
                      roleId: event.target.value,
                    }))
                  }
                  placeholder="roleId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={assignPermissions.permissionCodes}
                  onChange={(event) =>
                    setAssignPermissions((prev) => ({
                      ...prev,
                      permissionCodes: event.target.value,
                    }))
                  }
                  placeholder="IAM_MANAGE,CONFIG_VAT_EDIT"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <button
                  type="button"
                  onClick={replaceRolePermissions}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={iamLoading}
                >
                  Actualizar permisos
                </button>
              </div>
            </div>

            {iamStatus && <p className="text-sm text-slate-600">{iamStatus}</p>}
            {users.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(users, null, 2)}
              </pre>
            )}
            {roles.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(roles, null, 2)}
              </pre>
            )}
            {permissions.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(permissions, null, 2)}
              </pre>
            )}
            {branches.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(branches, null, 2)}
              </pre>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
