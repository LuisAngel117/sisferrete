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

type CatalogCategory = {
  id: string;
  code: string;
  name: string;
  isActive: boolean;
};

type CatalogBrand = {
  id: string;
  code: string;
  name: string;
  isActive: boolean;
};

type CatalogUom = {
  id: string;
  code: string;
  name: string;
  allowsDecimals: boolean;
  isActive: boolean;
};

type Product = {
  id: string;
  name: string;
  sku?: string | null;
  barcode?: string | null;
  categoryId?: string | null;
  brandId?: string | null;
  uomId: string;
  isActive: boolean;
};

type LookupResult = {
  matchType: string;
  productId: string;
  productName: string;
  variantId?: string | null;
  variantName?: string | null;
  uomId?: string | null;
  uomCode?: string | null;
};

type ProductVariant = {
  id: string;
  productId: string;
  name: string;
  barcode?: string | null;
  attributes?: Record<string, string>;
  isActive: boolean;
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

  const [catalogStatus, setCatalogStatus] = useState("");
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [categories, setCategories] = useState<CatalogCategory[]>([]);
  const [brands, setBrands] = useState<CatalogBrand[]>([]);
  const [uoms, setUoms] = useState<CatalogUom[]>([]);

  const [newCategory, setNewCategory] = useState({
    code: "",
    name: "",
    isActive: true,
  });
  const [updateCategory, setUpdateCategory] = useState({
    id: "",
    code: "",
    name: "",
    isActive: true,
  });

  const [newBrand, setNewBrand] = useState({
    code: "",
    name: "",
    isActive: true,
  });
  const [updateBrand, setUpdateBrand] = useState({
    id: "",
    code: "",
    name: "",
    isActive: true,
  });

  const [newUom, setNewUom] = useState({
    code: "",
    name: "",
    allowsDecimals: false,
    isActive: true,
  });
  const [updateUom, setUpdateUom] = useState({
    id: "",
    code: "",
    name: "",
    allowsDecimals: false,
    isActive: true,
  });

  const [productStatus, setProductStatus] = useState("");
  const [productLoading, setProductLoading] = useState(false);
  const [productQuery, setProductQuery] = useState("");
  const [lookupTerm, setLookupTerm] = useState("");
  const [products, setProducts] = useState<Product[]>([]);
  const [lookupResults, setLookupResults] = useState<LookupResult[]>([]);

  const [newProduct, setNewProduct] = useState({
    name: "",
    sku: "",
    barcode: "",
    categoryId: "",
    brandId: "",
    uomId: "",
    isActive: true,
  });

  const [updateProduct, setUpdateProduct] = useState({
    id: "",
    name: "",
    sku: "",
    barcode: "",
    categoryId: "",
    brandId: "",
    uomId: "",
    isActive: true,
  });

  const [variantStatus, setVariantStatus] = useState("");
  const [variants, setVariants] = useState<ProductVariant[]>([]);
  const [variantProductId, setVariantProductId] = useState("");
  const [newVariant, setNewVariant] = useState({
    productId: "",
    name: "",
    barcode: "",
    attributesText: "",
    isActive: true,
  });
  const [updateVariant, setUpdateVariant] = useState({
    variantId: "",
    name: "",
    barcode: "",
    attributesText: "",
    isActive: true,
  });

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

  const fetchCategories = async () => {
    setCatalogStatus("");
    setCatalogLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/catalog/categories`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: CatalogCategory[] = await res.json();
      setCategories(data);
      setCatalogStatus("Categorías cargadas.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const createCategory = async () => {
    setCatalogStatus("");
    setCatalogLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/catalog/categories`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify(newCategory),
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchCategories();
      setCatalogStatus("Categoría creada.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const updateCategoryData = async () => {
    setCatalogStatus("");
    if (!updateCategory.id) {
      setCatalogStatus("Debes indicar id de categoría.");
      return;
    }
    setCatalogLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/catalog/categories/${updateCategory.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            code: updateCategory.code,
            name: updateCategory.name,
            isActive: updateCategory.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchCategories();
      setCatalogStatus("Categoría actualizada.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const fetchBrands = async () => {
    setCatalogStatus("");
    setCatalogLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/catalog/brands`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: CatalogBrand[] = await res.json();
      setBrands(data);
      setCatalogStatus("Marcas cargadas.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const createBrand = async () => {
    setCatalogStatus("");
    setCatalogLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/catalog/brands`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify(newBrand),
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchBrands();
      setCatalogStatus("Marca creada.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const updateBrandData = async () => {
    setCatalogStatus("");
    if (!updateBrand.id) {
      setCatalogStatus("Debes indicar id de marca.");
      return;
    }
    setCatalogLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/catalog/brands/${updateBrand.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            code: updateBrand.code,
            name: updateBrand.name,
            isActive: updateBrand.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchBrands();
      setCatalogStatus("Marca actualizada.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const fetchUoms = async () => {
    setCatalogStatus("");
    setCatalogLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/catalog/uoms`, {
        headers: authHeaders,
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: CatalogUom[] = await res.json();
      setUoms(data);
      setCatalogStatus("Unidades cargadas.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const createUom = async () => {
    setCatalogStatus("");
    setCatalogLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/catalog/uoms`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify(newUom),
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchUoms();
      setCatalogStatus("Unidad creada.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const updateUomData = async () => {
    setCatalogStatus("");
    if (!updateUom.id) {
      setCatalogStatus("Debes indicar id de unidad.");
      return;
    }
    setCatalogLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/catalog/uoms/${updateUom.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            code: updateUom.code,
            name: updateUom.name,
            allowsDecimals: updateUom.allowsDecimals,
            isActive: updateUom.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchUoms();
      setCatalogStatus("Unidad actualizada.");
    } catch (err) {
      setCatalogStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setCatalogLoading(false);
    }
  };

  const fetchProducts = async () => {
    setProductStatus("");
    setProductLoading(true);
    try {
      const params = new URLSearchParams();
      if (productQuery.trim()) {
        params.set("query", productQuery.trim());
      }
      params.set("limit", "50");
      const res = await fetch(
        `${API_BASE}/api/admin/products?${params.toString()}`,
        {
          headers: authHeaders,
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: Product[] = await res.json();
      setProducts(data);
      setProductStatus("Productos cargados.");
    } catch (err) {
      setProductStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setProductLoading(false);
    }
  };

  const lookupProducts = async () => {
    setProductStatus("");
    if (!lookupTerm.trim()) {
      setProductStatus("Ingresa un término para lookup.");
      return;
    }
    setProductLoading(true);
    try {
      const params = new URLSearchParams();
      params.set("term", lookupTerm.trim());
      const res = await fetch(
        `${API_BASE}/api/admin/products/lookup?${params.toString()}`,
        {
          headers: authHeaders,
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: LookupResult[] = await res.json();
      setLookupResults(data);
      setProductStatus("Lookup completado.");
    } catch (err) {
      setProductStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setProductLoading(false);
    }
  };

  const createProduct = async () => {
    setProductStatus("");
    setProductLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/admin/products`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify({
          name: newProduct.name,
          sku: newProduct.sku,
          barcode: newProduct.barcode,
          categoryId: newProduct.categoryId || null,
          brandId: newProduct.brandId || null,
          uomId: newProduct.uomId || null,
          isActive: newProduct.isActive,
        }),
      });
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchProducts();
      setProductStatus("Producto creado.");
    } catch (err) {
      setProductStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setProductLoading(false);
    }
  };

  const updateProductData = async () => {
    setProductStatus("");
    if (!updateProduct.id) {
      setProductStatus("Debes indicar id del producto.");
      return;
    }
    setProductLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/products/${updateProduct.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            name: updateProduct.name,
            sku: updateProduct.sku,
            barcode: updateProduct.barcode,
            categoryId: updateProduct.categoryId || null,
            brandId: updateProduct.brandId || null,
            uomId: updateProduct.uomId || null,
            isActive: updateProduct.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchProducts();
      setProductStatus("Producto actualizado.");
    } catch (err) {
      setProductStatus(err instanceof Error ? err.message : "Error inesperado.");
    } finally {
      setProductLoading(false);
    }
  };

  const parseAttributes = (text: string) => {
    if (!text.trim()) {
      return null;
    }
    const result: Record<string, string> = {};
    text.split(",").forEach((raw) => {
      const pair = raw.trim();
      if (!pair) {
        return;
      }
      const [key, ...rest] = pair.split("=");
      if (!key || rest.length === 0) {
        return;
      }
      const value = rest.join("=").trim();
      if (value) {
        result[key.trim()] = value;
      }
    });
    return Object.keys(result).length ? result : null;
  };

  const fetchVariants = async () => {
    setVariantStatus("");
    if (!variantProductId.trim()) {
      setVariantStatus("Debes indicar productId.");
      return;
    }
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/products/${variantProductId}/variants`,
        {
          headers: authHeaders,
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      const data: ProductVariant[] = await res.json();
      setVariants(data);
      setVariantStatus("Variantes cargadas.");
    } catch (err) {
      setVariantStatus(err instanceof Error ? err.message : "Error inesperado.");
    }
  };

  const createVariant = async () => {
    setVariantStatus("");
    if (!newVariant.productId.trim()) {
      setVariantStatus("Debes indicar productId.");
      return;
    }
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/products/${newVariant.productId}/variants`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            name: newVariant.name,
            barcode: newVariant.barcode,
            attributes: parseAttributes(newVariant.attributesText),
            isActive: newVariant.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchVariants();
      setVariantStatus("Variante creada.");
    } catch (err) {
      setVariantStatus(err instanceof Error ? err.message : "Error inesperado.");
    }
  };

  const updateVariantData = async () => {
    setVariantStatus("");
    if (!updateVariant.variantId.trim()) {
      setVariantStatus("Debes indicar variantId.");
      return;
    }
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/variants/${updateVariant.variantId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            ...authHeaders,
          },
          body: JSON.stringify({
            name: updateVariant.name,
            barcode: updateVariant.barcode,
            attributes: parseAttributes(updateVariant.attributesText),
            isActive: updateVariant.isActive,
          }),
        }
      );
      if (!res.ok) {
        throw new Error(await res.text());
      }
      await fetchVariants();
      setVariantStatus("Variante actualizada.");
    } catch (err) {
      setVariantStatus(err instanceof Error ? err.message : "Error inesperado.");
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
            Pega un access token con permiso IAM_MANAGE, CONFIG_VAT_EDIT y
            CATALOG_MANAGE.
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

        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">Catálogo base</h2>
          <p className="mt-1 text-sm text-slate-500">
            Administra categorías, marcas y unidades de medida (UoM).
          </p>

          <div className="mt-4 grid gap-4">
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={fetchCategories}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={catalogLoading}
              >
                Ver categorías
              </button>
              <button
                type="button"
                onClick={fetchBrands}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={catalogLoading}
              >
                Ver marcas
              </button>
              <button
                type="button"
                onClick={fetchUoms}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={catalogLoading}
              >
                Ver unidades
              </button>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear categoría
                </h3>
                <input
                  value={newCategory.code}
                  onChange={(event) =>
                    setNewCategory((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="HERRAMIENTAS"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newCategory.name}
                  onChange={(event) =>
                    setNewCategory((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Herramientas"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newCategory.isActive}
                    onChange={(event) =>
                      setNewCategory((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={createCategory}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                  disabled={catalogLoading}
                >
                  Crear categoría
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar categoría
                </h3>
                <input
                  value={updateCategory.id}
                  onChange={(event) =>
                    setUpdateCategory((prev) => ({
                      ...prev,
                      id: event.target.value,
                    }))
                  }
                  placeholder="id categoría"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateCategory.code}
                  onChange={(event) =>
                    setUpdateCategory((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="HERRAMIENTAS"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateCategory.name}
                  onChange={(event) =>
                    setUpdateCategory((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Herramientas"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateCategory.isActive}
                    onChange={(event) =>
                      setUpdateCategory((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={updateCategoryData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={catalogLoading}
                >
                  Guardar cambios
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear marca
                </h3>
                <input
                  value={newBrand.code}
                  onChange={(event) =>
                    setNewBrand((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="BOSCH"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newBrand.name}
                  onChange={(event) =>
                    setNewBrand((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Bosch"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newBrand.isActive}
                    onChange={(event) =>
                      setNewBrand((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={createBrand}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                  disabled={catalogLoading}
                >
                  Crear marca
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar marca
                </h3>
                <input
                  value={updateBrand.id}
                  onChange={(event) =>
                    setUpdateBrand((prev) => ({
                      ...prev,
                      id: event.target.value,
                    }))
                  }
                  placeholder="id marca"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateBrand.code}
                  onChange={(event) =>
                    setUpdateBrand((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="BOSCH"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateBrand.name}
                  onChange={(event) =>
                    setUpdateBrand((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Bosch"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateBrand.isActive}
                    onChange={(event) =>
                      setUpdateBrand((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={updateBrandData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={catalogLoading}
                >
                  Guardar cambios
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear unidad
                </h3>
                <input
                  value={newUom.code}
                  onChange={(event) =>
                    setNewUom((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="KILOGRAMO"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newUom.name}
                  onChange={(event) =>
                    setNewUom((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Kilogramo"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newUom.allowsDecimals}
                    onChange={(event) =>
                      setNewUom((prev) => ({
                        ...prev,
                        allowsDecimals: event.target.checked,
                      }))
                    }
                  />
                  Permite decimales
                </label>
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newUom.isActive}
                    onChange={(event) =>
                      setNewUom((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={createUom}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                  disabled={catalogLoading}
                >
                  Crear unidad
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar unidad
                </h3>
                <input
                  value={updateUom.id}
                  onChange={(event) =>
                    setUpdateUom((prev) => ({
                      ...prev,
                      id: event.target.value,
                    }))
                  }
                  placeholder="id unidad"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateUom.code}
                  onChange={(event) =>
                    setUpdateUom((prev) => ({
                      ...prev,
                      code: event.target.value,
                    }))
                  }
                  placeholder="KILOGRAMO"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateUom.name}
                  onChange={(event) =>
                    setUpdateUom((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Kilogramo"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateUom.allowsDecimals}
                    onChange={(event) =>
                      setUpdateUom((prev) => ({
                        ...prev,
                        allowsDecimals: event.target.checked,
                      }))
                    }
                  />
                  Permite decimales
                </label>
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateUom.isActive}
                    onChange={(event) =>
                      setUpdateUom((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={updateUomData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={catalogLoading}
                >
                  Guardar cambios
                </button>
              </div>
            </div>

            {catalogStatus && (
              <p className="text-sm text-slate-600">{catalogStatus}</p>
            )}
            {categories.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(categories, null, 2)}
              </pre>
            )}
            {brands.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(brands, null, 2)}
              </pre>
            )}
            {uoms.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(uoms, null, 2)}
              </pre>
            )}
          </div>
        </section>

        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">Productos base</h2>
          <p className="mt-1 text-sm text-slate-500">
            CRUD de productos con búsqueda rápida (SKU/barcode/nombre).
          </p>

          <div className="mt-4 grid gap-4">
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={() => {
                  fetchCategories();
                  fetchBrands();
                  fetchUoms();
                }}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={productLoading || catalogLoading}
              >
                Cargar catálogos
              </button>
              <button
                type="button"
                onClick={fetchProducts}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                disabled={productLoading}
              >
                Buscar productos
              </button>
            </div>

            <div className="grid gap-3">
              <label className="text-sm font-medium text-slate-700">
                Búsqueda (nombre/SKU/barcode)
              </label>
              <input
                value={productQuery}
                onChange={(event) => setProductQuery(event.target.value)}
                placeholder="martillo / SKU-001 / 770123"
                className="h-10 rounded-md border border-slate-200 px-3 text-sm"
              />
            </div>

            <div className="grid gap-3">
              <label className="text-sm font-medium text-slate-700">
                Lookup rápido (POS)
              </label>
              <div className="flex flex-wrap gap-3">
                <input
                  value={lookupTerm}
                  onChange={(event) => setLookupTerm(event.target.value)}
                  placeholder="barcode o sku"
                  className="h-10 flex-1 rounded-md border border-slate-200 px-3 text-sm"
                />
                <button
                  type="button"
                  onClick={lookupProducts}
                  className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
                  disabled={productLoading}
                >
                  Lookup
                </button>
              </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear producto
                </h3>
                <input
                  value={newProduct.name}
                  onChange={(event) =>
                    setNewProduct((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Nombre"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newProduct.sku}
                  onChange={(event) =>
                    setNewProduct((prev) => ({
                      ...prev,
                      sku: event.target.value,
                    }))
                  }
                  placeholder="SKU (opcional)"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newProduct.barcode}
                  onChange={(event) =>
                    setNewProduct((prev) => ({
                      ...prev,
                      barcode: event.target.value,
                    }))
                  }
                  placeholder="Barcode (opcional)"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <select
                  value={newProduct.categoryId}
                  onChange={(event) =>
                    setNewProduct((prev) => ({
                      ...prev,
                      categoryId: event.target.value,
                    }))
                  }
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                >
                  <option value="">Categoría (opcional)</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.code} - {category.name}
                    </option>
                  ))}
                </select>
                <select
                  value={newProduct.brandId}
                  onChange={(event) =>
                    setNewProduct((prev) => ({
                      ...prev,
                      brandId: event.target.value,
                    }))
                  }
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                >
                  <option value="">Marca (opcional)</option>
                  {brands.map((brand) => (
                    <option key={brand.id} value={brand.id}>
                      {brand.code} - {brand.name}
                    </option>
                  ))}
                </select>
                <select
                  value={newProduct.uomId}
                  onChange={(event) =>
                    setNewProduct((prev) => ({
                      ...prev,
                      uomId: event.target.value,
                    }))
                  }
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                >
                  <option value="">UoM (requerida)</option>
                  {uoms.map((uom) => (
                    <option key={uom.id} value={uom.id}>
                      {uom.code} - {uom.name}
                    </option>
                  ))}
                </select>
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newProduct.isActive}
                    onChange={(event) =>
                      setNewProduct((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={createProduct}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                  disabled={productLoading}
                >
                  Crear producto
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar producto
                </h3>
                <input
                  value={updateProduct.id}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      id: event.target.value,
                    }))
                  }
                  placeholder="id producto"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateProduct.name}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Nombre"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateProduct.sku}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      sku: event.target.value,
                    }))
                  }
                  placeholder="SKU (opcional)"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateProduct.barcode}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      barcode: event.target.value,
                    }))
                  }
                  placeholder="Barcode (opcional)"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <select
                  value={updateProduct.categoryId}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      categoryId: event.target.value,
                    }))
                  }
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                >
                  <option value="">Categoría (opcional)</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.code} - {category.name}
                    </option>
                  ))}
                </select>
                <select
                  value={updateProduct.brandId}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      brandId: event.target.value,
                    }))
                  }
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                >
                  <option value="">Marca (opcional)</option>
                  {brands.map((brand) => (
                    <option key={brand.id} value={brand.id}>
                      {brand.code} - {brand.name}
                    </option>
                  ))}
                </select>
                <select
                  value={updateProduct.uomId}
                  onChange={(event) =>
                    setUpdateProduct((prev) => ({
                      ...prev,
                      uomId: event.target.value,
                    }))
                  }
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                >
                  <option value="">UoM (requerida)</option>
                  {uoms.map((uom) => (
                    <option key={uom.id} value={uom.id}>
                      {uom.code} - {uom.name}
                    </option>
                  ))}
                </select>
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateProduct.isActive}
                    onChange={(event) =>
                      setUpdateProduct((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={updateProductData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                  disabled={productLoading}
                >
                  Guardar cambios
                </button>
              </div>
            </div>

            {productStatus && (
              <p className="text-sm text-slate-600">{productStatus}</p>
            )}
            {products.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(products, null, 2)}
              </pre>
            )}
            {lookupResults.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(lookupResults, null, 2)}
              </pre>
            )}
          </div>
        </section>

        <section className="rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-base font-semibold">Variantes</h2>
          <p className="mt-1 text-sm text-slate-500">
            Crear y editar variantes por producto (atributos key=value).
          </p>

          <div className="mt-4 grid gap-4">
            <div className="flex flex-wrap gap-3">
              <input
                value={variantProductId}
                onChange={(event) => setVariantProductId(event.target.value)}
                placeholder="productId para listar variantes"
                className="h-10 flex-1 rounded-md border border-slate-200 px-3 text-sm"
              />
              <button
                type="button"
                onClick={fetchVariants}
                className="h-10 rounded-md border border-slate-300 px-4 text-sm font-semibold"
              >
                Ver variantes
              </button>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Crear variante
                </h3>
                <input
                  value={newVariant.productId}
                  onChange={(event) =>
                    setNewVariant((prev) => ({
                      ...prev,
                      productId: event.target.value,
                    }))
                  }
                  placeholder="productId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newVariant.name}
                  onChange={(event) =>
                    setNewVariant((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Nombre variante"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newVariant.barcode}
                  onChange={(event) =>
                    setNewVariant((prev) => ({
                      ...prev,
                      barcode: event.target.value,
                    }))
                  }
                  placeholder="Barcode (opcional)"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={newVariant.attributesText}
                  onChange={(event) =>
                    setNewVariant((prev) => ({
                      ...prev,
                      attributesText: event.target.value,
                    }))
                  }
                  placeholder="color=rojo, voltaje=110v"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={newVariant.isActive}
                    onChange={(event) =>
                      setNewVariant((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={createVariant}
                  className="h-10 rounded-md bg-slate-900 text-sm font-semibold text-white"
                >
                  Crear variante
                </button>
              </div>

              <div className="grid gap-3">
                <h3 className="text-sm font-semibold text-slate-700">
                  Actualizar variante
                </h3>
                <input
                  value={updateVariant.variantId}
                  onChange={(event) =>
                    setUpdateVariant((prev) => ({
                      ...prev,
                      variantId: event.target.value,
                    }))
                  }
                  placeholder="variantId"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateVariant.name}
                  onChange={(event) =>
                    setUpdateVariant((prev) => ({
                      ...prev,
                      name: event.target.value,
                    }))
                  }
                  placeholder="Nombre variante"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateVariant.barcode}
                  onChange={(event) =>
                    setUpdateVariant((prev) => ({
                      ...prev,
                      barcode: event.target.value,
                    }))
                  }
                  placeholder="Barcode (opcional)"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <input
                  value={updateVariant.attributesText}
                  onChange={(event) =>
                    setUpdateVariant((prev) => ({
                      ...prev,
                      attributesText: event.target.value,
                    }))
                  }
                  placeholder="color=rojo, voltaje=110v"
                  className="h-10 rounded-md border border-slate-200 px-3 text-sm"
                />
                <label className="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    type="checkbox"
                    checked={updateVariant.isActive}
                    onChange={(event) =>
                      setUpdateVariant((prev) => ({
                        ...prev,
                        isActive: event.target.checked,
                      }))
                    }
                  />
                  Activo
                </label>
                <button
                  type="button"
                  onClick={updateVariantData}
                  className="h-10 rounded-md border border-slate-300 text-sm font-semibold"
                >
                  Guardar cambios
                </button>
              </div>
            </div>

            {variantStatus && (
              <p className="text-sm text-slate-600">{variantStatus}</p>
            )}
            {variants.length > 0 && (
              <pre className="rounded-md bg-slate-100 p-3 text-xs text-slate-700">
                {JSON.stringify(variants, null, 2)}
              </pre>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
